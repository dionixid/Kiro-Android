package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class Setting(
    var id: String = "",
    var type: Type = Type.Info,
    var label: String = "",
    var value: Value = Value(),
    var isConfidential: Boolean = false
) : RObject() {

    enum class Type {
        Info,
        String,
        Float,
        Integer,
        Date,
        Time,
        WiFi,
        Latitude,
        Longitude,
        Elevation;

        companion object {
            fun of(value: Int): Type {
                if (value > values().lastIndex) {
                    return Info
                }
                return values()[value]
            }
        }
    }

    override val data: List<Value>
        get() = listOf(Value(id), Value(type.ordinal), Value(label), value)

    override fun assign(list: List<Value>) {
        if (list.size != 5) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isNumber() || !list[2].isString() || !list[4].isBoolean()) {
            isValid = false
            return
        }

        id = list[0].toString()
        type = Type.of(list[1].toInt())
        label = list[2].toString()
        value = list[3].copy()
        isConfidential = list[4].toBoolean()
    }
}