package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class SettingGroup(
    var name: String = "",
    var settings: List<Setting> = listOf()
): RObject() {

    override val data: List<Value>
        get() = listOf(Value(name), Value(settings))

    override fun assign(list: List<Value>) {
        if (list.size != 2) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isArray()) {
            isValid = false
            return
        }

        name = list[0].toString()
        settings = list[1].toList { it.toObject { Setting() } }
    }
}
