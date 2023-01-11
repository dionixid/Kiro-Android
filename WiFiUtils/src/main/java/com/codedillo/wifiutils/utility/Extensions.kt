package com.codedillo.wifiutils.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import com.codedillo.wifiutils.connection.Connection
import com.codedillo.wifiutils.connection.ConnectionErrorCode
import com.codedillo.wifiutils.connection.Security

internal fun String.quoted(): String {
    return if (startsWith("\"") && endsWith("\"")) this else "\"$this\""
}

internal fun Context.isAlreadyConnected(
    ssidOrBssid: String?,
    onResult: (isConnected: Boolean) -> Unit
) {
    if (Version.isAndroidQOrLater()) {
        Connection.isAlreadyConnected(ssidOrBssid, onResult)
        return
    }

    val wifiManager = getSystemService(WifiManager::class.java)
    val connectivityManager = getSystemService(ConnectivityManager::class.java)

    @Suppress("DEPRECATION")
    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state != NetworkInfo.State.CONNECTED) {
        onResult(false)
        return
    }

    if (ssidOrBssid == null) {
        onResult(true)
        return
    }

    @Suppress("DEPRECATION")
    val currentSSID = wifiManager.connectionInfo.ssid

    @Suppress("DEPRECATION")
    val currentBSSID = wifiManager.connectionInfo.bssid

    @Suppress("DEPRECATION")
    if (currentSSID != ssidOrBssid.quoted() && currentBSSID != ssidOrBssid) {
        onResult(false)
        return
    }

    Log.println("Already connected to: $currentSSID  BSSID: $currentBSSID")
    onResult(true)
}

internal fun Context.tryRegisterReceiver(receiver: BroadcastReceiver?, filter: IntentFilter) {
    receiver?.apply {
        try {
            registerReceiver(this, filter)
        } catch (_: Exception) {
        }
    }
}

internal fun Context.tryUnregisterReceiver(receiver: BroadcastReceiver?) {
    receiver?.apply {
        try {
            unregisterReceiver(this)
        } catch (_: Exception) {
        }
    }
}

private fun Context.connectPreAndroidQ(
    scanResult: ScanResult,
    password: String,
    onSuccess: (network: Network) -> Unit,
    onFailure: (error: ConnectionErrorCode) -> Unit,
    onLost: () -> Unit
) {
    val wifiManager = getSystemService(WifiManager::class.java)

    @Suppress("DEPRECATION")
    val config = WifiConfiguration().apply {
        SSID = scanResult.SSID.quoted()
        BSSID = scanResult.BSSID
        setSecurity(scanResult.security, password)
    }

    @Suppress("DEPRECATION")
    val networkId = wifiManager.addNetwork(config)
    if (networkId == -1) {
        onFailure(ConnectionErrorCode.COULD_NOT_CONNECT)
        return
    }

    try {
        @Suppress("DEPRECATION")
        for (wifiConfig in wifiManager.configuredNetworks) {
            if (wifiConfig.networkId != networkId) {
                wifiManager.disableNetwork(networkId)
            }
        }

        @Suppress("DEPRECATION")
        wifiManager.enableNetwork(networkId, true)
    } catch (_: SecurityException) {
        onFailure(ConnectionErrorCode.COULD_NOT_CONNECT)
        return
    }

    @Suppress("DEPRECATION")
    if (wifiManager.reassociate()) {
        val networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.println("AndroidPreQ connected to WiFi")
                onSuccess(network)
            }

            override fun onUnavailable() {
                Log.println("AndroidPreQ could not connect to WiFi")
                onFailure(ConnectionErrorCode.USER_CANCELLED)
            }

            override fun onLost(network: Network) {
                Log.println("AndroidPreQ connection lost")
                onLost()
                Connection.unwatch()
            }
        }

        Connection.watch(networkCallback)
        return
    }

    onFailure(ConnectionErrorCode.COULD_NOT_CONNECT)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.connectAndroidQ(
    scanResult: ScanResult,
    password: String,
    onSuccess: (network: Network) -> Unit,
    onFailure: (error: ConnectionErrorCode) -> Unit,
    onLost: () -> Unit
) {
    Connection.disconnect()

    val networkSpecifier = WifiNetworkSpecifier.Builder()
        .setSsid(scanResult.SSID)
        .setBssid(MacAddress.fromString(scanResult.BSSID))
        .setSecurity(scanResult.security, password)
        .build()

    val networkRequest = NetworkRequest.Builder().apply {
        addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        setNetworkSpecifier(networkSpecifier)
    }.build()

    val flag = if (Version.isAndroidSOrLater()) NetworkCallback.FLAG_INCLUDE_LOCATION_INFO else 0

    val networkCallback = object : NetworkCallback(flag) {
        override fun onAvailable(network: Network) {
            Log.println("AndroidQ+ connected to WiFi")
            isAlreadyConnected(scanResult.SSID) { isConnected ->
                if (isConnected) {
                    onSuccess(network)
                } else {
                    onFailure(ConnectionErrorCode.ANDROID_10_IMMEDIATELY_DROPPED_CONNECTION)
                }
            }
        }

        override fun onUnavailable() {
            Log.println("AndroidQ+ could not connect to WiFi")
            onFailure(ConnectionErrorCode.USER_CANCELLED)
        }

        override fun onLost(network: Network) {
            Log.println("AndroidQ+ connection lost")
            Connection.disconnect()
            onLost()
        }
    }

    Log.println("Connecting with AndroidQ+")
    Connection.requestNetwork(networkRequest, networkCallback)
}

internal fun Context.connect(
    scanResult: ScanResult,
    password: String,
    onSuccess: (network: Network) -> Unit,
    onFailure: (error: ConnectionErrorCode) -> Unit,
    onLost: () -> Unit
) {
    when {
        Version.isAndroidQOrLater() -> {
            connectAndroidQ(scanResult, password, onSuccess, onFailure, onLost)
        }
        else -> {
            connectPreAndroidQ(scanResult, password, onSuccess, onFailure, onLost)
        }
    }
}

@Suppress("DEPRECATION")
private fun WifiConfiguration.setSecurity(security: Security, password: String) {
    allowedAuthAlgorithms.clear()
    allowedGroupCiphers.clear()
    allowedKeyManagement.clear()
    allowedPairwiseCiphers.clear()
    allowedProtocols.clear()

    when (security) {
        Security.NONE -> {
            allowedKeyManagement.set(KeyMgmt.NONE)
            allowedProtocols.set(Protocol.RSN)
            allowedProtocols.set(Protocol.WPA)
            allowedPairwiseCiphers.set(PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(PairwiseCipher.TKIP)
            allowedGroupCiphers.set(GroupCipher.WEP40)
            allowedGroupCiphers.set(GroupCipher.WEP104)
            allowedGroupCiphers.set(GroupCipher.CCMP)
            allowedGroupCiphers.set(GroupCipher.TKIP)
        }
        Security.WEP -> {
            allowedKeyManagement.set(KeyMgmt.NONE)
            allowedProtocols.set(Protocol.RSN)
            allowedProtocols.set(Protocol.WPA)
            allowedAuthAlgorithms.set(AuthAlgorithm.OPEN)
            allowedAuthAlgorithms.set(AuthAlgorithm.SHARED)
            allowedPairwiseCiphers.set(PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(PairwiseCipher.TKIP)
            allowedGroupCiphers.set(GroupCipher.WEP40)
            allowedGroupCiphers.set(GroupCipher.WEP104)

            wepKeys[0] = if (isHexWepKey(password)) {
                password
            } else {
                password.quoted()
            }
        }
        Security.PSK -> {
            allowedProtocols.set(Protocol.RSN)
            allowedProtocols.set(Protocol.WPA)
            allowedKeyManagement.set(KeyMgmt.WPA_PSK)
            allowedPairwiseCiphers.set(PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(PairwiseCipher.TKIP)
            allowedGroupCiphers.set(GroupCipher.WEP40)
            allowedGroupCiphers.set(GroupCipher.WEP104)
            allowedGroupCiphers.set(GroupCipher.CCMP)
            allowedGroupCiphers.set(GroupCipher.TKIP)

            preSharedKey = if (password.matches(Regex("[0-9A-Fa-f]{64}"))) {
                password
            } else {
                password.quoted()
            }
        }
        Security.EAP -> {
            allowedProtocols.set(Protocol.RSN)
            allowedProtocols.set(Protocol.WPA)
            allowedGroupCiphers.set(GroupCipher.WEP40)
            allowedGroupCiphers.set(GroupCipher.WEP104)
            allowedGroupCiphers.set(GroupCipher.TKIP)
            allowedGroupCiphers.set(GroupCipher.CCMP)
            allowedPairwiseCiphers.set(PairwiseCipher.TKIP)
            allowedPairwiseCiphers.set(PairwiseCipher.CCMP)
            allowedKeyManagement.set(KeyMgmt.WPA_EAP)
            allowedKeyManagement.set(KeyMgmt.IEEE8021X)
            preSharedKey = password.quoted()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun WifiNetworkSpecifier.Builder.setSecurity(
    security: Security,
    password: String
) : WifiNetworkSpecifier.Builder {
    Log.println("Set WifiNetworkSpecifier security $security")
    when (security) {
        Security.EAP,
        Security.PSK -> setWpa2Passphrase(password)
        else -> {}
    }
    return this
}

private val ScanResult.security
    get() : Security {
        Log.println("ScanResult capabilities $capabilities")
        return when {
            capabilities.contains("WEP") -> Security.WEP
            capabilities.contains("PSK") -> Security.PSK
            capabilities.contains("EAP") -> Security.EAP
            else -> Security.NONE
        }
    }

private fun isHexWepKey(key: String): Boolean {
    return (key.length == 10 || key.length == 26 || key.length == 58) && key.matches(Regex("[0-9A-Fa-f]*"))
}
