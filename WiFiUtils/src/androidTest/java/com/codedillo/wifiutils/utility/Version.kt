package com.codedillo.wifiutils.utility

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi

object Version {

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun isAndroidQOrLater() : Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getPanelIntent() : Intent {
        return Intent(Settings.Panel.ACTION_WIFI)
    }

}