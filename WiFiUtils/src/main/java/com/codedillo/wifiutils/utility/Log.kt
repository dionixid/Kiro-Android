package com.codedillo.wifiutils.utility

import com.codedillo.wifiutils.WiFiUtils
import android.util.Log as NativeLog

internal object Log {
    var isEnabled = false

    fun println(message: String) {
        if (isEnabled) {
            NativeLog.println(NativeLog.DEBUG, WiFiUtils.TAG, message)
        }
    }
}