package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value
import java.time.format.DateTimeFormatter

data class Qiro(
    var name: Prayer.Name = Prayer.Name.Fajr,
    var durationMinutes: Int = 0,
    var surahList: List<Surah> = listOf()
): RObject() {

    fun getFormattedTime(prayer: Prayer): String {
        return prayer.getCalculatedTime().minusMinutes(durationMinutes.toLong()).format(formatter)
    }

    fun deepCopy(
        name: Prayer.Name = this.name,
        durationMinutes: Int = this.durationMinutes,
        surahList: List<Surah> = this.surahList.map { it.copy() }
    ): Qiro {
        return Qiro(name, durationMinutes, surahList)
    }

    override val data: List<Value>
        get() = listOf(Value(name.ordinal), Value(durationMinutes), Value(surahList))

    override fun assign(list: List<Value>) {
        if (list.size != 3) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber() || !list[2].isArray()) {
            isValid = false
            return
        }

        val nameIndex = list[0].toInt()
        if (nameIndex > Prayer.Name.values().lastIndex) {
            isValid = false
            return
        }

        name = Prayer.Name.values()[nameIndex]
        durationMinutes = list[1].toInt()
        surahList = list[2].toList { it.toObject { Surah() } }
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")

        fun getMaximumDuration(name: Prayer.Name): Int {
            return when (name) {
                Prayer.Name.Fajr -> 120
                Prayer.Name.Dhuhr -> 120
                Prayer.Name.Asr -> 60
                Prayer.Name.Maghrib -> 60
                Prayer.Name.Isha -> 30
            }
        }
    }
}