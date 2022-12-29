package com.codedillo.rttp

import com.codedillo.rttp.model.Auth
import com.codedillo.rttp.model.Value
import com.codedillo.rttp.model.Value.Raw
import com.codedillo.rttp.model.Value.Type
import org.junit.Assert
import org.junit.Test

class ExtractUnitTest {

    @Test
    fun extractInt() {
        Assert.assertEquals(Raw(Type.Number, 5L), Value(5).extract())
    }

    @Test
    fun extractFloat() {
        Assert.assertEquals(Raw(Type.Number, 5.0), Value(5f).extract())
    }

    @Test
    fun extractDouble() {
        Assert.assertEquals(Raw(Type.Number, 5.0), Value(5.0).extract())
    }

    @Test
    fun extractBooleanTrue() {
        Assert.assertEquals(Raw(Type.Boolean, true), Value(true).extract())
    }

    @Test
    fun extractBooleanFalse() {
        Assert.assertEquals(Raw(Type.Boolean, false), Value(false).extract())
    }

    @Test
    fun extractString() {
        Assert.assertEquals(Raw(Type.String, "Hello World"), Value("Hello World").extract())
    }

    @Test
    fun extractObject() {
        Assert.assertEquals(
            Raw(
                Type.Object,
                listOf(Raw(Type.String, "id"), Raw(Type.String, "name"), Raw(Type.String, "secret"))
            ), Value(Auth("id", "name", "secret")).extract()
        )
    }

    @Test
    fun extractArrayOfInt() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(Raw(Type.Number, 1L), Raw(Type.Number, 2L), Raw(Type.Number, 3L))
            ), Value(listOf(1, 2, 3)).extract()
        )
    }

    @Test
    fun extractArrayOfFloat() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(Raw(Type.Number, 1.0), Raw(Type.Number, 2.0), Raw(Type.Number, 3.0))
            ), Value(listOf(1f, 2f, 3f)).extract()
        )
    }

    @Test
    fun extractArrayOfDouble() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(Raw(Type.Number, 1.0), Raw(Type.Number, 2.0), Raw(Type.Number, 3.0))
            ), Value(listOf(1.0, 2.0, 3.0)).extract()
        )
    }

    @Test
    fun extractArrayOfBoolean() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(Raw(Type.Boolean, true), Raw(Type.Boolean, false), Raw(Type.Boolean, true))
            ), Value(listOf(true, false, true)).extract()
        )
    }

    @Test
    fun extractArrayOfString() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(Raw(Type.String, "A"), Raw(Type.String, "B"), Raw(Type.String, "C"))
            ), Value(listOf("A", "B", "C")).extract()
        )
    }

    @Test
    fun extractArrayOfObject() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(
                    Raw(Type.Object, listOf(
                        Raw(Type.String, "id"),
                        Raw(Type.String, "name"),
                        Raw(Type.String, "secret")
                    )),
                    Raw(Type.Object, listOf(
                        Raw(Type.String, "id"),
                        Raw(Type.String, "name"),
                        Raw(Type.String, "secret")
                    )),
                    Raw(Type.Object, listOf(
                        Raw(Type.String, "id"),
                        Raw(Type.String, "name"),
                        Raw(Type.String, "secret")
                    ))
                )
            ), Value(listOf(
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret")
            )).extract()
        )
    }

    @Test
    fun extractArrayOfArrayOfInt() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(
                    Raw(Type.Array,
                        listOf(Raw(Type.Number, 1L), Raw(Type.Number, 2L), Raw(Type.Number, 3L))),
                    Raw(Type.Array,
                        listOf(Raw(Type.Number, 1L), Raw(Type.Number, 2L), Raw(Type.Number, 3L))),
                    Raw(Type.Array,
                        listOf(Raw(Type.Number, 1L), Raw(Type.Number, 2L), Raw(Type.Number, 3L))),
                )
            ), Value(listOf(
                listOf(1, 2, 3),
                listOf(1, 2, 3),
                listOf(1, 2, 3)
            )).extract()
        )
    }

    @Test
    fun extractArrayOfArrayOfArrayOfInt() {
        Assert.assertEquals(
            Raw(
                Type.Array,
                listOf(
                    Raw(Type.Array, listOf(
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L)))
                    )),
                    Raw(Type.Array, listOf(
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L)))
                    )),
                    Raw(Type.Array, listOf(
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L))),
                        Raw(Type.Array,
                            listOf(Raw(Type.Number, 1L),
                                Raw(Type.Number, 2L),
                                Raw(Type.Number, 3L)))
                    )),
                )
            ), Value(listOf(
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
            )).extract()
        )
    }
}