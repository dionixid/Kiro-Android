package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value
import java.time.format.DateTimeFormatter

data class Qiro(
    var name: Prayer.Name = Prayer.Name.Fajr,
    var durationMinutes: Int = 0,
    var surahIds: List<Int> = listOf(),
    var volume: Int = 0
): RObject() {

    fun getFormattedTime(prayer: Prayer): String {
        return prayer.getCalculatedTime().minusMinutes(durationMinutes.toLong()).format(formatter)
    }

    fun deepCopy(
        name: Prayer.Name = this.name,
        durationMinutes: Int = this.durationMinutes,
        surahIds: List<Int> = this.surahIds.map { it },
        volume: Int = this.volume
    ): Qiro {
        return Qiro(name, durationMinutes, surahIds, volume)
    }

    override val data: List<Value>
        get() = listOf(Value(name.ordinal), Value(durationMinutes), Value(surahIds), Value(volume))

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
        surahIds = list[2].toList { it.toInt() }
        volume = list[3].toInt()
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}