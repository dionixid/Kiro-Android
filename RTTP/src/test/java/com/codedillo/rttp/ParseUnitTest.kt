package com.codedillo.rttp

import com.codedillo.rttp.model.Auth
import com.codedillo.rttp.model.Value
import org.junit.Test
import org.junit.Assert.*

class ParseUnitTest {

    @Test
    fun parseInt() {
        assertEquals(5, Value.parse("5").toInt())
    }

    @Test
    fun parseFloat() {
        assertEquals(5f, Value.parse("5.0").toFloat())
    }

    @Test
    fun parseDouble() {
        assertEquals(5.0, Value.parse("5.0").toDouble(), 0.0)
    }

    @Test
    fun parseBooleanTrue() {
        assertEquals(true, Value.parse("true").toBoolean())
    }

    @Test
    fun parseBooleanFalse() {
        assertEquals(false, Value.parse("false").toBoolean())
    }

    @Test
    fun parseString() {
        assertEquals("Hello World", Value.parse("\"Hello World\"").toString())
    }

    @Test
    fun parseObject() {
        assertEquals(
            Auth("id", "name", "secret"),
            Value.parse("{\"id\",\"name\",\"secret\"}").toObject { Auth() }
        )
    }

    @Test
    fun parseArrayOfInt() {
        assertEquals(
            listOf(1, 2, 3),
            Value.parse("[1,2,3]").toList { it.toInt() }
        )
    }

    @Test
    fun parseArrayOfFloat() {
        assertEquals(
            listOf(1f, 2f, 3f),
            Value.parse("[1.0,2.0,3.0]").toList { it.toFloat() }
        )
    }

    @Test
    fun parseArrayOfDouble() {
        assertEquals(
            listOf(1.0, 2.0, 3.0),
            Value.parse("[1.0,2.0,3.0]").toList { it.toDouble() }
        )
    }

    @Test
    fun parseArrayOfBoolean() {
        assertEquals(
            listOf(true, false, true),
            Value.parse("[true,false,true]").toList { it.toBoolean() }
        )
    }

    @Test
    fun parseArrayOfString() {
        assertEquals(
            listOf("A", "B", "C"),
            Value.parse("[\"A\",\"B\",\"C\"]").toList { it.toString() }
        )
    }

    @Test
    fun parseArrayOfObject() {
        assertEquals(
            listOf(
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret")
            ),
            Value
                .parse("[{\"id\",\"name\",\"secret\"},{\"id\",\"name\",\"secret\"},{\"id\",\"name\",\"secret\"}]")
                .toList { it.toObject { Auth() } }
        )
    }

    @Test
    fun parseArrayOfArrayOfInt() {
        assertEquals(
            listOf(
                listOf(1, 2, 3),
                listOf(1, 2, 3),
                listOf(1, 2, 3)
            ),
            Value.parse("[[1,2,3],[1,2,3],[1,2,3]]").toList { it.toList { v -> v.toInt() } }
        )
    }

    @Test
    fun parseArrayOfArrayOfArrayOfInt() {
        assertEquals(
            listOf(
                listOf(
                    listOf(1, 2, 3),
                    listOf(1, 2, 3),
                    listOf(1, 2, 3)
                ),
                listOf(
                    listOf(1, 2, 3),
                    listOf(1, 2, 3),
                    listOf(1, 2, 3)
                ),
                listOf(
                    listOf(1, 2, 3),
                    listOf(1, 2, 3),
                    listOf(1, 2, 3)
                )
            ),
            Value
                .parse("[[[1,2,3],[1,2,3],[1,2,3]],[[1,2,3],[1,2,3],[1,2,3]],[[1,2,3],[1,2,3],[1,2,3]]]")
                .toList { a ->
                    a.toList { b ->
                        b.toList { c ->
                            c.toInt()
                        }
                    }
                }
        )
    }
}