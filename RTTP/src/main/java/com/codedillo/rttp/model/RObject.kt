package com.codedillo.rttp.model

abstract class RObject {
    abstract val data: List<Value>

    open var isValid: Boolean = true
        protected set

    abstract fun assign(list: List<Value>)

    fun serialize(): String {
        if (data.isEmpty()) {
            return "{}"
        }

        val sb = StringBuilder("{")
        sb.append(data[0].serialize())

        for (i in 1..data.lastIndex) {
            sb.append(",")
            sb.append(data[i].serialize())
        }

        sb.append("}")
        return sb.toString()
    }
}