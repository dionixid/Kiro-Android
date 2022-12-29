package com.codedillo.rttp.model

import com.codedillo.rttp.utility.serialize
import java.lang.Character.isDigit
import java.math.BigDecimal
import java.math.BigInteger

class Value() {

    private var value: String = "null"
    var type: Type = Type.Null
        private set

    override fun equals(other: Any?): Boolean {
        if (other !is Value) {
            return false
        }

        return type == other.type && value == other.value
    }

    override fun hashCode(): Int {
        return type.hashCode() + value.hashCode()
    }

    fun copy(): Value {
        return Value(type, value)
    }

    private constructor(type: Type, value: String) : this() {
        this.type = type
        this.value = value
    }

    constructor(value: String?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.String
        }
    }

    constructor(value: Number?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Number
        }
    }

    constructor(value: BigInteger?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Number
        }
    }

    constructor(value: BigDecimal?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Number
        }
    }

    constructor(value: Boolean?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Boolean
        }
    }

    constructor(value: RObject?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Object
        }
    }

    constructor(value: List<Any>?) : this() {
        if (value != null) {
            this.value = value.serialize()
            type = Type.Array
        }
    }

    constructor(raw: Raw) : this() {
        type = raw.type

        value = buildString {
            val rawValue = raw.value

            when (raw.type) {
                Type.Object,
                Type.Array,
                -> {
                    if (raw.type == Type.Object) {
                        append("{")
                    } else {
                        append("[")
                    }

                    val values = mutableListOf<Raw>()

                    if (rawValue is List<*>) {
                        rawValue.forEach {
                            if (it is Raw) {
                                values.add(it)
                            }
                        }
                    }

                    if (values.isNotEmpty()) {
                        append(Value(values[0]).serialize())
                    }

                    for (i in 1..values.lastIndex) {
                        append(",")
                        append(Value(values[i]).serialize())
                    }

                    if (raw.type == Type.Object) {
                        append("}")
                    } else {
                        append("]")
                    }
                }

                Type.Null -> {
                    append("null")
                }

                else -> {
                    append(rawValue.serialize())
                }
            }
        }
    }

    fun serialize(): String {
        return value
    }

    fun <T : RObject> toObject(creator: () -> T): T {
        val entity = creator()
        if (type != Type.Object) {
            return entity
        }
        entity.assign(parse())
        return entity
    }

    fun <T> toList(creator: (value: Value) -> T): List<T> {
        val result = listOf<T>()
        if (type != Type.Array) {
            return result
        }
        val list = parse()
        return list.map { creator(it) }
    }

    override fun toString(): String {
        if (isObject() || isArray() || isString()) {
            return value.replace(Regex("(?<!\\\\)\""), "")
        }
        return value
    }

    fun toInt(): Int {
        return value.toIntOrNull() ?: 0
    }

    fun toLong(): Long {
        return value.toLongOrNull() ?: 0
    }

    fun toBigInteger(): BigInteger {
        return value.toBigIntegerOrNull() ?: BigInteger.ZERO
    }


    fun toFloat(): Float {
        return value.toFloatOrNull() ?: 0f
    }

    fun toDouble(): Double {
        return value.toDoubleOrNull() ?: 0.0
    }

    fun toBigDecimal(): BigDecimal {
        return value.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    fun toBoolean(): Boolean {
        return value == "true"
    }

    fun isObject(): Boolean {
        return type == Type.Object
    }

    fun isArray(): Boolean {
        return type == Type.Array
    }

    fun isString(): Boolean {
        return type == Type.String
    }

    fun isNumber(): Boolean {
        return type == Type.Number
    }

    fun isBoolean(): Boolean {
        return type == Type.Boolean
    }

    fun isNull(): Boolean {
        return type == Type.Null
    }

    fun extract(): Raw {
        if (isObject()) {
            val list = mutableListOf<Raw>()
            parse().forEach {
                list.add(it.extract())
            }
            return Raw(type, list)
        }

        if (isArray()) {
            val list = mutableListOf<Raw>()
            parse().forEach {
                list.add(it.extract())
            }
            return Raw(type, list)
        }

        if (isNumber()) {
            val number = toBigDecimal()
            return Raw(
                type,
                if (number.rem(BigDecimal.ONE) == BigDecimal.ZERO) {
                    if (number < Long.MAX_VALUE.toBigDecimal() && number > Long.MIN_VALUE.toBigDecimal()) {
                        toLong()
                    } else {
                        toBigInteger()
                    }
                } else {
                    if (number < Double.MAX_VALUE.toBigDecimal() && number > Double.MIN_VALUE.toBigDecimal()) {
                        toDouble()
                    } else {
                        number
                    }
                }
            )
        }

        if (isBoolean()) {
            return Raw(type, toBoolean())
        }

        return Raw(type, toString())
    }

    private fun findClosingObjectBracket(src: String, start: Int): Int {
        var count = 0
        for (i in start..src.length) {
            if (src[i] == '{') {
                count++
            }

            if (src[i] == '}') {
                count--
            }

            if (count == 0) {
                return i
            }
        }
        return -1
    }

    private fun findClosingArrayBracket(src: String, start: Int): Int {
        var count = 0
        for (i in start..src.length) {
            if (src[i] == '[') {
                count++
            }

            if (src[i] == ']') {
                count--
            }

            if (count == 0) {
                return i
            }
        }
        return -1
    }

    private fun findClosingQuote(src: String, start: Int): Int {
        for (i in start..src.length) {
            if (src[i] == '"' && src[i - 1] != '\\') {
                return i
            }
        }
        return -1
    }

    private fun isLiteral(element: String): Boolean {
        return element == "true" || element == "false" || element == "null"
    }

    private fun isNumber(element: String): Boolean {
        if (element.isEmpty()) {
            return false
        }

        if (!isDigit(element[0]) && element[0] != '-' && element[0] != '+' && element[0] != '.') {
            return false
        }

        var period = 0
        var exponent = 0

        for (i in 0..element.lastIndex) {
            if (element[i] == '.') {
                period++
                if (exponent > 0) {
                    return false
                }
                if (period > 1) {
                    return false
                }
                continue
            }

            if (element[i] == 'e' || element[i] == 'E') {
                exponent++
                if (exponent > 1) {
                    return false
                }
                continue
            }

            if (element[i] == '+' && i != 0) {
                return false
            }

            if (element[i] == '-') {
                if (i != 0 && element[i - 1] != 'e' && element[i - 1] != 'E') {
                    return false
                }
                continue
            }

            if (!isDigit(element[i])) {
                return false
            }
        }

        return true
    }

    private fun parse(): List<Value> {
        val result = mutableListOf<Value>()
        var index = 1

        while (index < value.lastIndex) {
            if (value[index] == ',') {
                index++
                continue
            }

            if (value[index] == '{') {
                val closeIndex = findClosingObjectBracket(value, index)
                if (closeIndex == -1) {
                    result.clear()
                    return result
                }
                result.add(parse(value.substring(index, closeIndex + 1)))
                index = closeIndex + 1
                continue
            }

            if (value[index] == '[') {
                val closeIndex = findClosingArrayBracket(value, index)
                if (closeIndex == -1) {
                    result.clear()
                    return result
                }
                result.add(parse(value.substring(index, closeIndex + 1)))
                index = closeIndex + 1
                continue
            }

            if (value[index] == '"') {
                val closeIndex = findClosingQuote(value, index + 1)
                if (closeIndex == -1) {
                    result.clear()
                    return result
                }
                result.add(parse(value.substring(index, closeIndex + 1)))
                index = closeIndex + 1
                continue
            }

            var closeIndex = value.indexOf(",", index)
            if (closeIndex == -1) {
                closeIndex = value.lastIndex
            }

            val element = value.substring(index, closeIndex)
            index = closeIndex + 1

            if (!isLiteral(element) && !isNumber(element)) {
                result.clear()
                return result
            }

            result.add(parse(element))
        }

        return result
    }

    enum class Type {
        Object,
        Array,
        String,
        Number,
        Boolean,
        Null
    }

    data class Raw(
        val type: Type,
        var value: Any,
    ) {
        companion object {
            fun of(type: Type): Raw {
                return when (type) {
                    Type.Object,
                    Type.Array,
                    -> Raw(type, listOf(of(Type.Null)))
                    Type.String -> Raw(type, "")
                    Type.Number -> Raw(type, 0)
                    Type.Boolean -> Raw(type, false)
                    Type.Null -> Raw(type, "null")
                }
            }
        }
    }

    companion object {

        internal fun parse(serialized: String): Value {
            if (serialized.startsWith("{") && serialized.endsWith("}")) {
                return Value(Type.Object, serialized)
            }

            if (serialized.startsWith("[") && serialized.endsWith("]")) {
                return Value(Type.Array, serialized)
            }

            if (serialized.startsWith("\"") && serialized.endsWith("\"")) {
                return Value(Type.String, serialized)
            }

            if (serialized == "true" || serialized == "false") {
                return Value(Type.Boolean, serialized)
            }

            if (serialized.toDoubleOrNull() != null) {
                return Value(Type.Number, serialized)
            }

            return Value()
        }

    }

}