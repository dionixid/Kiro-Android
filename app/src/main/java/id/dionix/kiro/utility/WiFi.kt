package id.dionix.kiro.utility

import android.content.Context
import android.net.Network
import android.net.wifi.ScanResult
import com.codedillo.wifiutils.WiFiUtils

object WiFi {
    private const val BSSID_ID = "52:4F:58"

    fun initialize(context: Context) {
        WiFiUtils.initialize(context)
    }

    fun isConnected(ssid: String, onResult: (isConnected: Boolean) -> Unit) {
        WiFiUtils.isConnected(ssid) {
            runMain {
                onResult(it)
            }
        }
    }

    fun enableWiFi(listener: (isEnabled: Boolean) -> Unit) {
        WiFiUtils.enable(listener)
    }

    fun disableWiFi(listener: (isEnabled: Boolean) -> Unit) {
        WiFiUtils.disable(listener)
    }

    fun scan(listener: (results: List<ScanResult>) -> Unit) {
        WiFiUtils.scan { results ->
            listener(
                results.filter {
                    it.BSSID.trim()
                        .replace("\"", "")
                        .uppercase()
                        .startsWith(BSSID_ID)
                }
            )
        }
    }

    fun connect(
        ssid: String,
        mac: String,
        password: String,
        onResult: (isSuccess: Boolean, network: Network?) -> Unit,
        onLost: () -> Unit
    ) {
        WiFiUtils.connect(
            ssid,
            mac,
            password,
            onSuccess = {
                runMain {
                    onResult(true, it)
                }
            },
            onFailure = {
                runMain {
                    onResult(false, null)
                }
            },
            onLost = {
                runMain {
                    onLost()
                }
            }
        )
    }

    fun disconnect() {
        WiFiUtils.disconnect()
    }

    fun enableLog() {
        WiFiUtils.setDebugEnabled(true)
    }

    fun disableLog() {
        WiFiUtils.setDebugEnabled(false)
    }

}