package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class SettingGroup(
    var name: String = "",
    var settings: List<Setting> = listOf()
): RObject() {

    fun setSetting(setting: Setting) {
        val oldSettings = settings.toMutableList()
        val idx = oldSettings.indexOfFirst { it.id == setting.id }

        if (idx != -1) {
            oldSettings[idx] = setting
            settings = oldSettings
        }
    }

    fun getSetting(id: String) : Setting? {
        return settings.find { it.id == id }
    }

    fun getSetting(type: Setting.Type) : Setting? {
        return settings.find { it.type == type }
    }

    fun contains(type: Setting.Type) : Boolean {
        return settings.indexOfFirst { it.type == type } != -1
    }

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
