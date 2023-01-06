package id.dionix.kiro.utility

import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

object UDP {
    private const val DEVICE_SIGNATURE = "_kiro._tcp"
    private const val PROTOCOL_SIGNATURE = "local."
    private const val DEVICE_PORT = 46525

    private var mCoroutine: CoroutineScope? = null
    private val mBuffer = ByteArray(64)
    private var mSocket: DatagramSocket? = null

    private var wifiManager: WifiManager? = null
    private var connectivityManager: ConnectivityManager? = null

    private var mOnResult: (name: String, ip: String) -> Unit = { _, _, -> }

    fun initialize(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            connectivityManager =
                connectivityManager ?: context.getSystemService(ConnectivityManager::class.java)
        } else {
            wifiManager = wifiManager ?: context.getSystemService(WifiManager::class.java)
        }

        initializeUDP()
    }

    fun scan(onResult: (name: String, ip: String) -> Unit) {
        mOnResult = onResult

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            tryRun {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            }
            tryRun {
                connectivityManager?.requestNetwork(request, networkCallback)
            }
        } else {
            val gateway = wifiManager?.gateway
            if (gateway?.isValidIp() == false) {
                return
            }
            val currentAddress = wifiManager?.ipAddress
            pingAll(gateway ?: "") {
                getValidDevices(it.filter { ip -> ip != currentAddress })
            }
        }
    }

    fun rebind() {
        tryRun {
            mCoroutine?.cancel()
        }

        tryRun {
            mSocket?.close()
        }

        initializeUDP()
    }

    fun detach() {
        mOnResult = { _, _ -> }
    }

    fun release() {
        tryRun {
            mCoroutine?.cancel()
        }

        tryRun {
            mSocket?.close()
        }

        tryRun {
            Thread.shutdown()
        }
    }

    private fun initializeUDP() {
        tryRun {
            mCoroutine?.cancel()
            mSocket?.close()
        }

        mSocket = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = 5000
            bind(InetSocketAddress(DEVICE_PORT))
        }

        mCoroutine = CoroutineScope(Dispatchers.IO)
        mCoroutine?.launch {
            while (isActive) {
                kotlin.runCatching {
                    tryRun {
                        val packet = DatagramPacket(mBuffer, mBuffer.size)

                        mSocket?.receive(packet)
                        val sb = StringBuilder()

                        for (byte in packet.data) {
                            if (byte in 0x20..0x7e) {
                                sb.append(byte.toInt().toChar())
                            }
                        }

                        if (sb.toString().isNotEmpty()) {
                            parseMessage(sanitizeMessage(sb.toString()))
                        }
                    }
                }

                delay(100)
            }
        }
    }

    private fun ping(
        subnet: String,
        from: Int,
        to: Int,
        callback: ((address: String?) -> Unit)? = null
    ) {
        tryRun {
            for (i in from..to) {
                val address = "$subnet.$i"
                if (InetAddress.getByName(address).isReachable(1000)) {
                    callback?.invoke(address)
                } else {
                    callback?.invoke(null)
                }
            }
        }
    }

    private fun pingAll(
        gateway: String,
        div: Int = 1,
        callback: ((list: ArrayList<String>) -> Unit)? = null
    ) {
        val addresses = ArrayList<String>()
        if (!gateway.isValidIp()) {
            callback?.invoke(addresses)
            return
        }
        val subnet = gateway.substring(0, gateway.lastIndexOf("."))
        val counter = Counter(255)
        for (i in 0..floor(255.0 / div).toInt()) {
            Thread.execute {
                ping(subnet, (i * div) + 1, ((i + 1) * div).coerceAtMost(255)) {
                    it?.let { address ->
                        addresses.add(address)
                    }
                    counter.decrement()
                    if (counter.value == 0) {
                        callback?.invoke(addresses)
                    }
                }
            }
        }
    }

    private fun getValidDevices(addresses: List<String>) {
        tryRun {
            addresses.forEach { ip: String? ->
                if (ip != null) {
                    mSocket?.send(
                        DatagramPacket(
                            DEVICE_SIGNATURE.toByteArray(),
                            DEVICE_SIGNATURE.length,
                            InetSocketAddress(ip, DEVICE_PORT)
                        )
                    )
                }
            }
        }
    }

    private fun parseMessage(message: String) {
        if (message.startsWith(DEVICE_SIGNATURE) && message.endsWith(PROTOCOL_SIGNATURE)) {
            val name = Regex("(?<=name:)(.*?)(?=\\.)").find(message)?.groupValues?.get(0)
            val id = Regex("(?<=id:)(.*?)(?=\\.)").find(message)?.groupValues?.get(0)
            val ip = Regex("(?<=ip:)(.*?)(?=\\.local)").find(message)?.groupValues?.get(0)

            if (name != null && id != null && ip != null) {
                mOnResult(String.format(Locale.US, "%s %s", name, id), ip)
            }
        }
    }

    private fun sanitizeMessage(message: String): String {
        val start = message.indexOf(DEVICE_SIGNATURE)
        val end = message.indexOf(PROTOCOL_SIGNATURE) + PROTOCOL_SIGNATURE.length
        return message.substring(start, end)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(net: Network, link: LinkProperties) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val gateway = link.dhcpServerAddress?.hostAddress.toString()
                if (!gateway.isValidIp()) {
                    return
                }
                val currentAddress =
                    link.linkAddresses[link.linkAddresses.lastIndex].address.hostName
                pingAll(gateway) {
                    getValidDevices(it.filter { ip -> ip != currentAddress })
                }
            }
        }
    }

    private data class Counter(private var count: Int) {
        val value: Int
            @Synchronized get() {
                return count
            }

        @Synchronized
        fun decrement() {
            count--
        }
    }

    private fun String.isValidIp(): Boolean {
        return Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!\$)|\$)){4}\$").matches(this)
    }

    private val WifiManager.gateway: String
        get() {
            @Suppress("DEPRECATION")
            val address = this.dhcpInfo.gateway
            return "${address and 0xff}.${address shr 8 and 0xff}.${address shr 16 and 0xff}.${address shr 24 and 0xff}"
        }

    private val WifiManager.ipAddress: String
        get() {
            @Suppress("DEPRECATION")
            val address = this.dhcpInfo.ipAddress
            return "${address and 0xff}.${address shr 8 and 0xff}.${address shr 16 and 0xff}.${address shr 24 and 0xff}"
        }

}