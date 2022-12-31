package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class QiroAudio(
    var isPlaying: Boolean = false,
    var surahId: Int = 0
) : RObject() {

    override val data: List<Value>
        get() = listOf(Value(isPlaying), Value(surahId))

    override fun assign(list: List<Value>) {
        if (list.size != 2) {
            isValid = false
            return
        }

        if (!list[0].isBoolean() || !list[1].isNumber()) {
            isValid = false
            return
        }

        isPlaying = list[0].toBoolean()
        surahId = list[0].toInt()
    }

}