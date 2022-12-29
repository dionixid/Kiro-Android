package id.dionix.kiro.model

import com.codedillo.rttp.model.RObject
import com.codedillo.rttp.model.Value

data class Device(
    var id: String = "",
    var name: String = ""
) : RObject() {

    override val data: List<Value>
        get() = listOf(Value(id), Value(id))

    override fun assign(list: List<Value>) {
        if (list.size != 2) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isString()) {
            isValid = false
            return
        }

        id = list[0].toString()
        name = list[1].toString()
    }
}