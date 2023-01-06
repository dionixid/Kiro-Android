package com.codedillo.rttp

import android.net.Network
import android.os.CountDownTimer
import com.codedillo.rttp.model.*
import com.codedillo.rttp.model.Message.Companion.ALL_RECIPIENTS
import com.codedillo.rttp.model.Message.Companion.ALL_TOPICS
import com.codedillo.rttp.model.Message.Companion.CHANNELS_TOPIC
import com.codedillo.rttp.model.Message.Companion.SERVER_ID
import com.codedillo.rttp.model.Message.Companion.SUBSCRIBERS_TOPIC
import com.codedillo.rttp.utility.*
import com.neovisionaries.ws.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RTTPClient(
    host: String = "",
    port: Int = 0,
    name: String = "",
    id: String = "",
) {

    private var mHost: String = host
    private var mPort: Int = port
    private var mId: String = id
    private var mName: String = name
    private var mSecret: String = ""
    private var mChannel: Channel = Channel()

    private lateinit var mClient: WebSocket
    private var mNetwork: Network? = null
    private val mFactory: WebSocketFactory
        get() {
            return mNetwork?.let {
                WebSocketFactory().apply {
                    socketFactory = it.socketFactory
                }
            } ?: WebSocketFactory()
        }

    private val mChannels: MutableList<Channel> = mutableListOf()
    private val mSubscribers: MutableList<Subscriber> = mutableListOf()
    private val mMessageHandlers: MutableMap<String, MessageHandler> = mutableMapOf()

    private var mIsRegistered = false
    private var mIsAutoConnect = false
    private var mIsChangingChannel = false

    private var mOnJoinListener: () -> Unit = {}
    private var mOnLeaveListener: () -> Unit = {}
    private var mOnAuthTimeoutListener: () -> Unit = {}
    private var mOnAuthListener: (success: Boolean) -> Unit = {}

    private var mOnChannelsUpdatedListener: (channels: List<Channel>) -> Unit = {}
    private var mOnSubscribersUpdatedListener: (channels: List<Subscriber>) -> Unit = {}

    /**
     * Check if the client is currently connected.
     */
    val isOpen: Boolean get() = this::mClient.isInitialized && mClient.isOpen

    /**
     * Get currently connected channel.
     */
    val currentChannel: Channel get() = mChannel

    /**
     * Get the channels information that is available on the server.
     */
    val channels: List<Channel> get() = mChannels

    /**
     * Get the subscribers information that currently joined to the same channel.
     */
    val subscribers: List<Subscriber> get() = mSubscribers

    fun bind(network: Network?) {
        mNetwork = network
        join(mChannel.name, mSecret)
    }

    /**
     * Join to a channel.
     *
     * @param channel is the channel to join.
     * @param secret is the secret key to authenticate with.
     */
    fun join(channel: String = GLOBAL_CHANNEL, secret: String = "") {
        if (mHost.isEmpty() || mPort == 0) {
            return
        }

        if (channel != mChannel.name) {
            mIsChangingChannel = true
        }

        leave()

        if (isOpen) {
            mIsRegistered = false
            if (!mIsChangingChannel) {
                mOnLeaveListener()
            }
        }

        mChannel.name = channel
        mSecret = secret

        val url = "ws://$mHost:$mPort/rttp/${channel.lowercase()}"
        mClient = mFactory.createSocket(url, 5000)
        mClient.addListener(socketListener)
        mClient.connectAsynchronously()

        mIsAutoConnect = true
    }

    /**
     * Rejoin to the current channel.
     */
    fun rejoin() {
        join(mChannel.name, mSecret)
    }

    /**
     * Leave the channel.
     */
    fun leave() {
        if (this::mClient.isInitialized) {
            mClient.clearListeners()
            reconnectTimer.cancel()

            val client = mClient
            CoroutineScope(Dispatchers.IO).launch {
                while (client.state == WebSocketState.CREATED || client.state == WebSocketState.CONNECTING) {
                    delay(1000)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    client.disconnect()
                }
            }
        }
        mIsAutoConnect = false
    }

    /**
     * Send message to another client.
     *
     * @param recipientId is the destination client id.
     * @param topic is the topic of the message.
     * @param action is the [Message.Action] of the message.
     * @param payload is the payload of the message.
     */
    fun send(recipientId: String, topic: String, action: Message.Action, payload: Value) {
        if (!mChannel.hasTopic(topic)) {
            return
        }
        mClient.sendText(Message(mId, recipientId, topic, action, payload).serialize())
    }

    /**
     * Broadcast message to all clients.
     *
     * @param topic is the topic of the message.
     * @param action is the [Message.Action] of the message.
     * @param payload is the payload of the message.
     */
    fun publish(topic: String, action: Message.Action, payload: Value) {
        if (!mChannel.hasTopic(topic)) {
            return
        }
        mClient.sendText(Message(mId, ALL_RECIPIENTS, topic, action, payload).serialize())
    }

    /**
     * Subscribe to a topic.
     *
     * @param topic is the topic be subscribed.
     * @param handler is the [MessageHandler] to be called when a message received.
     */
    fun on(topic: String, handler: MessageHandler) {
        mMessageHandlers[topic] = handler
    }

    /**
     * Unsubscribe from a topic.
     *
     * @param topic is the topic to be unsubscribed.
     */
    fun off(topic: String) {
        mMessageHandlers.remove(topic)
    }

    /**
     * Set a listener to be called when the client is successfully connected.
     *
     * @param listener is the handler to be called.
     */
    fun onJoin(listener: () -> Unit) {
        mOnJoinListener = listener
    }

    /**
     * Set a listener to be called when the client has been disconnected.
     *
     * @param listener is the handler to be called.
     */
    fun onLeave(listener: () -> Unit) {
        mOnLeaveListener = listener
    }

    /**
     * Set a listener to be called when an authentication result received.
     *
     * @param listener is the handler to be called.
     */
    fun onAuth(listener: (success: Boolean) -> Unit) {
        mOnAuthListener = listener
    }

    /**
     * Set a listener to be called when an authentication process timed out.
     *
     * @param listener is the handler to be called.
     */
    fun onAuthTimeout(listener: () -> Unit) {
        mOnAuthTimeoutListener = listener
    }

    /**
     * Set a listener to be called when the channels has been updated.
     *
     * @param listener is the handler to be called.
     */
    fun onChannelsUpdated(listener: (channels: List<Channel>) -> Unit) {
        mOnChannelsUpdatedListener = listener
    }

    /**
     * Set a listener to be called when the subscribers has been updated.
     *
     * @param listener is the handler to be called.
     */
    fun onSubscribersUpdated(listener: (subscribers: List<Subscriber>) -> Unit) {
        mOnSubscribersUpdatedListener = listener
    }

    /**
     * Set the id of the client.
     *
     * @param id is the id of the client.
     */
    fun setId(id: String) {
        mId = id
    }

    /**
     * Set the host of the server.
     *
     * @param host is the host of the server.
     */
    fun setHost(host: String) {
        mHost = host
    }

    /**
     * Set the port of the server.
     *
     * @param port is the port of the server.
     */
    fun setPort(port: Int) {
        mPort = port
    }

    /**
     * Set the name to be identified by other client.
     *
     * @param name is the name of the client.
     */
    fun setName(name: String) {
        mName = name
    }

    private fun register() {
        mClient.sendBinary(Auth(mId, mName, mSecret).serialize().toByteArray())
    }

    private val socketListener = object : WebSocketAdapter() {
        override fun onConnected(
            websocket: WebSocket?,
            headers: MutableMap<String, MutableList<String>>?,
        ) {
            mClient.pongInterval = 15000
            mOnJoinListener()
            mIsChangingChannel = false

            if (mChannel.name.isNotBlank()) {
                register()
                authTimer.start()
            }
        }

        override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
            if (binary == null) {
                return
            }

            authTimer.cancel()
            mIsRegistered = String(binary) == "auth-ok"
            mOnAuthListener(mIsRegistered)

            if (!mIsRegistered) {
                leave()
            }
        }

        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            if (text == null) {
                return
            }

            val message = Value.parse(text).toObject { Message() }
            if (!message.isValid) {
                return
            }

            if (message.topic == CHANNELS_TOPIC) {
                if (message.senderId != SERVER_ID) {
                    return
                }

                val list = message.payload.toList { it.toObject { Channel() } }
                mChannels.clear()

//                Log.println(Log.ASSERT, "RTTP Client", message.payload.toString())
                list.forEach { channel ->
                    if (channel.isValid) {
                        mChannels.add(channel)
                    }

                    if (channel.name == mChannel.name) {
                        mChannel = channel
                    }
                }
                mOnChannelsUpdatedListener(mChannels)
                return
            }

            if (message.topic == SUBSCRIBERS_TOPIC) {
                if (message.senderId != SERVER_ID) {
                    return
                }

                val list = message.payload.toList { it.toObject { Subscriber() } }
                mSubscribers.clear()

                list.forEach { subscriber ->
                    if (subscriber.isValid) {
                        mSubscribers.add(subscriber)
                    }
                }
                mOnSubscribersUpdatedListener(mSubscribers)
                return
            }

            if (message.recipientId != mId) {
                return
            }

            mMessageHandlers.forEach {
                if (it.key == message.topic || it.key == ALL_TOPICS) {
                    it.value(message)
                }
            }
        }

        override fun onDisconnected(
            websocket: WebSocket?,
            serverCloseFrame: WebSocketFrame?,
            clientCloseFrame: WebSocketFrame?,
            closedByServer: Boolean,
        ) {
            reconnectTimer.start()
            mIsRegistered = false
            if (!mIsChangingChannel) {
                mOnLeaveListener()
            }
        }

        override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
            reconnectTimer.start()
        }
    }

    private val reconnectTimer = object : CountDownTimer(5000, 5000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            join(mChannel.name, mSecret)
        }
    }

    private val authTimer = object : CountDownTimer(5000, 5000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            leave()
            mOnAuthTimeoutListener()
        }
    }

    companion object {
        private const val GLOBAL_CHANNEL = ""
    }

}
