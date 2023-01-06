package id.dionix.kiro.utility

import android.content.Context
import android.net.wifi.ScanResult
import com.thanosfisherman.wifiutils.WifiConnectorBuilder.WifiUtilsBuilder
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionErrorCode
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionSuccessListener

object WiFi {
    private const val BSSID_ID = "52:4F:58"
    private lateinit var wifiUtils: WifiUtilsBuilder

    val isConnected: Boolean get() = wifiUtils.isWifiConnected

    fun initialize(context: Context) {
        wifiUtils = WifiUtils.withContext(context)
    }

    fun isConnected(ssid: String): Boolean {
        return wifiUtils.isWifiConnected(ssid)
    }

    fun enableWiFi(listener: (isSuccess: Boolean) -> Unit) {
        wifiUtils.enableWifi(listener)
    }

    fun disableWiFi() {
        wifiUtils.disableWifi()
    }

    fun scan(listener: (results: List<ScanResult>) -> Unit) {
        wifiUtils.scanWifi { results ->
            listener(
                results.filter {
                    it.BSSID.trim()
                        .replace("\"", "")
                        .uppercase()
                        .startsWith(BSSID_ID)
                }
            )
        }.start()
    }

    fun connect(
        ssid: String,
        mac: String,
        password: String,
        listener: (isSuccess: Boolean) -> Unit
    ) {
        wifiUtils.connectWith(ssid, mac, password)
            .setTimeout(30000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    listener(true)
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    listener(false)
                }
            })
            .start()
    }

    fun disconnect(listener: (isSuccess: Boolean) -> Unit = {}) {
        wifiUtils.disconnect(object : DisconnectionSuccessListener {
            override fun success() {
                listener(true)
            }

            override fun failed(errorCode: DisconnectionErrorCode) {
                listener(false)
            }
        })
    }

    fun enableLog() {
        WifiUtils.enableLog(true)
    }

    fun disableLog() {
        WifiUtils.enableLog(false)
    }

}