package com.codedillo.wifiutils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.net.Network
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.CountDownTimer
import com.codedillo.wifiutils.connection.Connection
import com.codedillo.wifiutils.connection.ConnectionErrorCode
import com.codedillo.wifiutils.utility.*
import com.codedillo.wifiutils.utility.Log
import com.codedillo.wifiutils.utility.Version
import com.codedillo.wifiutils.utility.isAlreadyConnected
import com.codedillo.wifiutils.utility.tryRegisterReceiver

object WiFiUtils {
    const val TAG = "WiFiUtils"

    private lateinit var mWifiManager: WifiManager

    private var mStartActivityImpl: (intent: Intent) -> Unit = {}
    private var mRegisterReceiverImpl: (
        receiver: BroadcastReceiver,
        filter: IntentFilter
    ) -> Unit = { _, _ -> }

    private var mUnregisterReceiverImpl: (receiver: BroadcastReceiver) -> Unit = {}
    private var mConnectImpl: (scanResult: ScanResult, password: String) -> Unit = { _, _ -> }
    private var mIsConnectedImpl: (ssid: String?) -> Unit = {}

    private var mOnState: (isEnabled: Boolean) -> Unit = {}
    private var mOnScan: (results: List<ScanResult>) -> Unit = {}
    private var mOnConnectSuccess: (network: Network) -> Unit = {}
    private var mOnConnectFailure: (error: ConnectionErrorCode) -> Unit = {}
    private var mOnLost: () -> Unit = {}
    private var mOnIsConnected: (isConnected: Boolean) -> Unit = {}

    private var mSsid = ""
    private var mBssid = ""
    private var mPassword = ""

    fun setDebugEnabled(enabled: Boolean) {
        Log.isEnabled = enabled
    }

    fun initialize(context: Context) {
        Connection.initialize(context)
        mWifiManager = context.getSystemService(WifiManager::class.java)

        mStartActivityImpl = {
            context.applicationContext.startActivity(it)
        }

        mRegisterReceiverImpl = { receiver, filter ->
            context.tryRegisterReceiver(receiver, filter)
        }

        mUnregisterReceiverImpl = {
            context.tryUnregisterReceiver(it)
        }

        mIsConnectedImpl = {
            context.isAlreadyConnected(it, mOnIsConnected)
        }

        mConnectImpl = { scanResult, password ->
            context.connect(
                scanResult,
                password,
                onSuccess = {
                    mConnectionTimer.cancel()
                    mOnConnectSuccess(it)
                },
                onFailure = {
                    mConnectionTimer.cancel()
                    mOnConnectFailure(it)
                },
                onLost = {
                    mOnLost()
                }
            )
        }
    }

    fun connect(
        ssid: String,
        bssid: String,
        password: String,
        timeout: Long = 30000,
        onSuccess: (network: Network) -> Unit = {},
        onFailure: (error: ConnectionErrorCode) -> Unit = {},
        onLost: () -> Unit
    ) {
        mSsid = ssid
        mBssid = bssid
        mPassword = password
        mOnConnectSuccess = onSuccess
        mOnConnectFailure = onFailure
        mOnLost = onLost

        enable {
            if (it) {
                @Suppress("DEPRECATION")
                mWifiManager.startScan()

                unregisterReceiver(mConnectReceiver)
                registerReceiver(mConnectReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

                mConnectionTimer = makeTimer(timeout) {
                    unregisterReceiver(mConnectReceiver)
                    onFailure(ConnectionErrorCode.TIMEOUT_OCCURRED)
                }
                mConnectionTimer.start()
            } else {
                onFailure(ConnectionErrorCode.COULD_NOT_ENABLE_WIFI)
            }
        }
    }

    fun disconnect() {
        Connection.disconnect()
    }

    fun scan(onResult: (results: List<ScanResult>) -> Unit) {
        mOnScan = onResult

        @Suppress("DEPRECATION")
        mWifiManager.startScan()

        unregisterReceiver(mScanReceiver)
        registerReceiver(mScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    fun enable(onResult: (isEnabled: Boolean) -> Unit = {}) {
        if (mWifiManager.isWifiEnabled) {
            onResult(true)
            return
        }

        mOnState = onResult

        if (Version.isAndroidQOrLater()) {
            startActivity(
                Version.getPanelIntent().apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            )
            unregisterReceiver(mStateReceiver)
            registerReceiver(mStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        } else {
            @Suppress("DEPRECATION")
            if (mWifiManager.setWifiEnabled(true)) {
                unregisterReceiver(mStateReceiver)
                registerReceiver(
                    mStateReceiver,
                    IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
                )
            } else {
                Log.println("Could not enable WiFi")
                mOnState(false)
            }
        }
    }

    fun disable(onResult: (isEnabled: Boolean) -> Unit = {}) {
        if (mWifiManager.isWifiEnabled) {
            onResult(true)
            return
        }

        mOnState = onResult

        if (Version.isAndroidQOrLater()) {
            startActivity(
                Version.getPanelIntent().apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            )
            unregisterReceiver(mStateReceiver)
            registerReceiver(mStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        } else {
            @Suppress("DEPRECATION")
            if (mWifiManager.setWifiEnabled(false)) {
                unregisterReceiver(mStateReceiver)
                registerReceiver(
                    mStateReceiver,
                    IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
                )
            } else {
                Log.println("Could not enable WiFi")
                mOnState(false)
            }
        }
    }

    fun isConnected(ssid: String? = null, onResult: (isConnected: Boolean) -> Unit) {
        mOnIsConnected = onResult
        mIsConnectedImpl(ssid)
    }

    private val mStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unregisterReceiver(this)
            when (intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                WifiManager.WIFI_STATE_ENABLED -> mOnState(true)
                else -> mOnState(false)
            }
        }
    }

    private val mScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unregisterReceiver(this)
            mOnScan(mWifiManager.scanResults)
        }
    }

    private val mConnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unregisterReceiver(this)
            val scanResult =
                mWifiManager.scanResults.find { it.SSID == mSsid && it.BSSID == mBssid }
            if (scanResult == null) {
                mOnConnectFailure(ConnectionErrorCode.DID_NOT_FIND_NETWORK_BY_SCANNING)
                return
            }
            mConnectImpl(scanResult, mPassword)
        }
    }

    private var mConnectionTimer = makeTimer(1000) {}

    private fun makeTimer(millis: Long, run: () -> Unit): CountDownTimer {
        return object : CountDownTimer(millis, millis) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                run()
            }
        }
    }

    private fun startActivity(intent: Intent) {
        mStartActivityImpl(intent)
    }

    private fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        mRegisterReceiverImpl(receiver, filter)
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        mUnregisterReceiverImpl(receiver)
    }

}