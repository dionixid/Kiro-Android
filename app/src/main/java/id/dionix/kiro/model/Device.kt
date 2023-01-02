package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class Device(
    var id: String = "",
    var name: String = "",
    var version: String = ""
) : RObject() {

    override val data: List<Value>
        get() = listOf(Value(id), Value(name), Value(version))

    override fun assign(list: List<Value>) {
        if (list.size != 3) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isString() || !list[2].isString()) {
            isValid = false
            return
        }

        id = list[0].toString()
        name = list[1].toString()
        version = list[2].toString()
    }

}