package com.codedillo.rttp.model

data class Subscriber(
    var id: String = "",
    var name: String = ""
): RObject() {

    override val data: List<Value>
        get() = listOf(Value(id), Value(name))

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