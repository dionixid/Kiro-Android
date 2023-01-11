package id.dionix.kiro.database

import android.net.Network
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codedillo.rttp.RTTPClient
import com.codedillo.rttp.model.Message
import com.codedillo.rttp.model.Value
import id.dionix.kiro.utility.*

class DataRepository : DefaultLifecycleObserver {

    private val mDevice: RTTPClient = RTTPClient()

    private val mMutableIsConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    val isConnected: LiveData<Boolean> get() = mMutableIsConnected

    private val mMutableIsAuthenticated: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAuthenticated: LiveData<Boolean> get() = mMutableIsAuthenticated

    fun join(password: String) {
        mIsJoined = true
        mDevice.join(CHANNEL, password)
    }

    fun rejoin() {
        mDevice.rejoin()
    }

    fun bind(network: Network?) {
        UDP.bind(network)
        mDevice.bind(network)
        if (mIsJoined) {
            mDevice.rejoin()
        }
    }

    fun leave() {
        mIsJoined = false
        mDevice.leave()
    }

    fun setClientID(name: String, id: String) {
        mDevice.setName(name)
        mDevice.setId(id)
    }

    fun setServer(host: String, port: Int) {
        mDevice.setHost(host)
        mDevice.setPort(port)
    }

    fun onTopic(topic: String, handler: (message: Message) -> Unit) {
        mDevice.on(topic, handler)
    }

    fun send(topic: String, action: Message.Action, payload: Value) {
        mDevice.send(Message.SERVER_ID, topic, action, payload)
    }

    init {
        mDevice.onJoin {
            runMain {
                mMutableIsConnected.value = true
            }
        }

        mDevice.onLeave {
            runMain {
                mMutableIsConnected.value = false
            }
        }

        mDevice.onAuth {
            runMain {
                mMutableIsAuthenticated.value = it
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (mIsJoined) {
            mDevice.rejoin()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        mDevice.leave()
    }

    private var mIsJoined = false

    companion object {
        private const val CHANNEL = "kiro"
    }
}