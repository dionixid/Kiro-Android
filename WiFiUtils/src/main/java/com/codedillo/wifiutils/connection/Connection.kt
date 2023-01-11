package com.codedillo.wifiutils.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.codedillo.wifiutils.utility.Log
import com.codedillo.wifiutils.utility.Version
import com.codedillo.wifiutils.utility.quoted

object Connection {
    private lateinit var mConnectivityManager: ConnectivityManager
    private var mNetworkCallback: NetworkCallback? = null
    private var mConnectionCheckCallback: NetworkCallback? = null
    private var mConnectionWatcherCallback: NetworkCallback? = null

    fun initialize(context: Context) {
        mConnectivityManager = context.getSystemService(ConnectivityManager::class.java)
    }

    fun requestNetwork(
        networkRequest: NetworkRequest,
        networkCallback: NetworkCallback
    ) {
        mNetworkCallback = networkCallback
        mConnectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun disconnect() {
        try {
            mNetworkCallback?.apply {
                mConnectivityManager.unregisterNetworkCallback(this)
                Log.println("WiFi has been disconnected")
            }
        } catch (_: Exception) {}
    }

    fun watch(networkCallback: NetworkCallback) {
        try {
            mConnectionWatcherCallback?.apply {
                mConnectivityManager.unregisterNetworkCallback(this)
            }
        } catch (_: Exception) {}

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        mConnectionWatcherCallback = networkCallback
        mConnectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unwatch() {
        try {
            mConnectionWatcherCallback?.apply {
                mConnectivityManager.unregisterNetworkCallback(this)
            }
        } catch (_: Exception) {}
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isAlreadyConnected(ssidOrBssid: String?, onResult: (isConnected: Boolean) -> Unit) {
        try {
            mConnectionCheckCallback?.apply {
                mConnectivityManager.unregisterNetworkCallback(this)
            }
        } catch (_: Exception) {}

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val flag = if (Version.isAndroidSOrLater()) NetworkCallback.FLAG_INCLUDE_LOCATION_INFO else 0

        val networkCallback = object : NetworkCallback(flag) {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                try {
                    mConnectionCheckCallback?.apply {
                        mConnectivityManager.unregisterNetworkCallback(this)
                    }
                } catch (_: Exception) {}

                val transportInfo = networkCapabilities.transportInfo
                if (transportInfo !is WifiInfo) {
                    return
                }

                if (ssidOrBssid == null) {
                    onResult(true)
                    return
                }

                Log.println("Currently connected to ${transportInfo.ssid} [${transportInfo.bssid}] ")
                onResult(transportInfo.ssid == ssidOrBssid.quoted() || transportInfo.bssid == ssidOrBssid)
            }
        }

        mConnectionCheckCallback = networkCallback
        mConnectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

}