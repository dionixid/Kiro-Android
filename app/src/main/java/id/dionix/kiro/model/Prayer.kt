package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Prayer(
    var name: Name = Name.Fajr,
    var time: LocalTime = LocalTime.now(),
    var offset: Int = 0,
) : RObject() {

    enum class Name {
        Fajr,
        Dhuhr,
        Asr,
        Maghrib,
        Isha;

        fun next(): Name {
            var index = ordinal + 1
            if (index > values().lastIndex) {
                index = 0
            }
            return values()[index]
        }
    }

    fun getCalculatedTime(): LocalTime {
        return time.plusMinutes(offset.toLong())
    }

    fun getFormattedTime(): String {
        return getCalculatedTime().format(formatter)
    }

    fun getFormattedOffset(): String {
        return if (offset >= 0) {
            "+$offset"
        } else {
            offset.toString()
        }
    }

    override val data: List<Value>
        get() = listOf(Value(name.ordinal), Value(time.toSecondOfDay() / 60), Value(offset))

    override fun assign(list: List<Value>) {
        if (list.size != 3) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber() || !list[2].isNumber()) {
            isValid = false
            return
        }

        val nameIndex = list[0].toInt()
        if (nameIndex > Name.values().lastIndex) {
            isValid = false
            return
        }

        name = Name.values()[nameIndex]
        time = LocalTime.ofSecondOfDay(list[1].toLong() * 60L)
        offset = list[2].toInt()
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}