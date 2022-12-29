package com.codedillo.rttp.model

data class Auth(
    var id: String = "",
    var name: String = "",
    var secret: String = ""
): RObject() {

    override val data: List<Value>
        get() = listOf(Value(id), Value(name), Value(secret))

    override fun assign(list: List<Value>) {
        if (list.size != 3) {
            isValid = false
            return
        }

        if (!list[0].isString()|| !list[1].isString() || !list[2].isString()) {
            isValid = false
            return
        }

        id = list[0].toString()
        name = list[1].toString()
        secret = list[2].toString()
    }
}
