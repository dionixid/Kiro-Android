package com.codedillo.rttp.model

data class Channel(
    var name: String = "",
    var topics: List<String> = listOf()
): RObject() {

    override val data: List<Value>
        get() = listOf(Value(name), Value(topics))

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
        topics = list[1].toList { it.toString() }
    }

    fun hasTopic(name: String): Boolean {
        return topics.contains(name)
    }
}
