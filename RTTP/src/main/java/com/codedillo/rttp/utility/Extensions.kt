package com.codedillo.rttp.utility

import com.codedillo.rttp.model.Message
import com.codedillo.rttp.model.RObject

typealias MessageHandler = (message: Message) -> Unit

internal fun Any?.serialize(): String {
    if (this == null) {
        return "null"
    }

    if (this is String) {
        return StringBuilder("\"")
            .append(replace(Regex("(?<!\\\\)\""), "\\\""))
            .append("\"")
            .toString()
    }

    if (this is RObject) {
        return this.serialize()
    }

    if (this is List<*>) {
        if (isEmpty()) {
            return "[]"
        }
        return this.serialize()
    }

    return toString()
}

internal fun List<Any?>.serialize(): String {
    if (isEmpty()) {
        return "[]"
    }

    val result = StringBuilder("[")
    result.append(get(0).serialize())

    for (i in 1..lastIndex) {
        result.append(",")
        result.append(get(i).serialize())
    }

    result.append("]")
    return result.toString()
}
