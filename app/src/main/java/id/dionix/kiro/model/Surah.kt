package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class Surah(
    var id: Int = 0,
    var volume: Int = 20
) : RObject() {

    override val data: List<Value>
        get() = listOf(Value(id), Value(volume))

    override fun assign(list: List<Value>) {
        if (list.size != 2) {
            isValid = false
            return
        }

        if (!list[0].isNumber() || !list[1].isNumber()) {
            isValid = false
            return
        }

        id = list[0].toInt()
        volume = list[1].toInt()
    }
}