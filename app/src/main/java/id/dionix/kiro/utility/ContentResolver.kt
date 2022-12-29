package id.dionix.kiro.utility

import android.content.Context
import id.dionix.kiro.R

object ContentResolver {

    fun getString(context: Context, value: String): String {
        return when (value.lowercase()) {
            "connect" -> context.getString(R.string.connect)
            "disconnect" -> context.getString(R.string.disconnect)
            "connected" -> context.getString(R.string.connected)
            "disconnected" -> context.getString(R.string.disconnected)
            "date and time" -> context.getString(R.string.date_and_time)
            "time" -> context.getString(R.string.time)
            "date" -> context.getString(R.string.date)
            "location" -> context.getString(R.string.location)
            "latitude" -> context.getString(R.string.latitude)
            "elevation" -> context.getString(R.string.elevation)
            "longitude" -> context.getString(R.string.longitude)
            "about" -> context.getString(R.string.about)
            "security" -> context.getString(R.string.security)
            "version" -> context.getString(R.string.version)
            else -> value
        }
    }
}