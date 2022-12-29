package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value
import java.time.format.DateTimeFormatter

data class Qiro(
    var name: Prayer.Name = Prayer.Name.Fajr,
    var durationMinutes: Int = 0,
    var surahId: Int = 0,
    var volume: Int = 0
): RObject() {

    fun getFormattedTime(prayer: Prayer): String {
        return prayer.getCalculatedTime().minusMinutes(durationMinutes.toLong()).format(formatter)
    }

    override val data: List<Value>
        get() = listOf(Value(name.ordinal), Value(durationMinutes), Value(surahId), Value(volume))

    override fun assign(list: List<Value>) {
        if (list.size != 4) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber() || !list[2].isNumber() || !list[3].isNumber()) {
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
        surahId = list[2].toInt()
        volume = list[3].toInt()
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}