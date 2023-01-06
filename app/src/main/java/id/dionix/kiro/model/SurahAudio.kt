package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class SurahAudio(
    var id: Int = 0,
    var volume: Int = 0,
    var isPaused: Boolean = false,
    var isPlaying: Boolean = false
) : RObject() {

    fun toSurah(): Surah {
        return Surah(id, volume)
    }

    override val data: List<Value>
        get() = listOf(Value(id), Value(volume), Value(isPaused), Value(isPlaying))

    override fun assign(list: List<Value>) {
        if (list.size != 4) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber() || !list[2].isBoolean() || !list[3].isBoolean()) {
            isValid = false
            return
        }

        id = list[0].toInt()
        volume = list[1].toInt()
        isPaused = list[2].toBoolean()
        isPlaying = list[3].toBoolean()
    }
}