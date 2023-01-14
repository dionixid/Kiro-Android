package id.dionix.kiro.utility

import android.content.Context
import id.dionix.kiro.R
import id.dionix.kiro.database.SurahViewModel
import id.dionix.kiro.model.Surah
import id.dionix.kiro.model.SurahProperties
import java.time.DayOfWeek

object ContentResolver {
    private var mAllSurah: List<SurahProperties> = listOf()

    fun updateSurahList(allSurah: List<SurahProperties>) {
        mAllSurah = allSurah
    }

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

    fun getDayName(context: Context, dayOfWeek: DayOfWeek): String {
        return context.getString(
            when (dayOfWeek) {
                DayOfWeek.MONDAY -> R.string.monday
                DayOfWeek.TUESDAY -> R.string.tuesday
                DayOfWeek.WEDNESDAY -> R.string.wednesday
                DayOfWeek.THURSDAY -> R.string.thursday
                DayOfWeek.FRIDAY -> R.string.friday
                DayOfWeek.SATURDAY -> R.string.saturday
                else -> R.string.sunday
            }
        )
    }

    fun getSurahProperties(surah: Surah): SurahProperties {
        return mAllSurah.find {
            it.id == surah.id
        }?.apply {
            volume = surah.volume
        } ?: SurahProperties(name = "Untitled", id = surah.id, volume = surah.volume)
    }
}