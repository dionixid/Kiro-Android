package com.codedillo.rttp

import com.codedillo.rttp.model.Auth
import com.codedillo.rttp.model.Value
import org.junit.Assert
import org.junit.Test

class ConstructFromRawUnitTest {

    @Test
    fun constructInt() {
        Assert.assertEquals(Value(5), Value(Value.Raw(Value.Type.Number, 5L)))
    }

    @Test
    fun constructFloat() {
        Assert.assertEquals(Value(5f), Value(Value.Raw(Value.Type.Number, 5.0)))
    }

    @Test
    fun constructDouble() {
        Assert.assertEquals(Value(5.0), Value(Value.Raw(Value.Type.Number, 5.0)))
    }

    @Test
    fun constructBooleanTrue() {
        Assert.assertEquals(Value(true), Value(Value.Raw(Value.Type.Boolean, true)))
    }

    @Test
    fun constructBooleanFalse() {
        Assert.assertEquals(Value(false), Value(Value.Raw(Value.Type.Boolean, false)))
    }

    @Test
    fun constructString() {
        Assert.assertEquals(Value("Hello World"),
            Value(Value.Raw(Value.Type.String, "Hello World")))
    }

    @Test
    fun constructObject() {
        Assert.assertEquals(
            Value(Auth("id", "name", "secret")),
            Value(
                Value.Raw(
                    Value.Type.Object,
                    listOf(
                        Value.Raw(Value.Type.String, "id"),
                        Value.Raw(Value.Type.String, "name"),
                        Value.Raw(Value.Type.String, "secret")
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfInt() {
        Assert.assertEquals(
            Value(listOf(1, 2, 3)),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(Value.Raw(
                        Value.Type.Number, 1L),
                        Value.Raw(Value.Type.Number, 2L),
                        Value.Raw(Value.Type.Number, 3L)
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfFloat() {
        Assert.assertEquals(
            Value(listOf(1f, 2f, 3f)),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(Value.Type.Number, 1.0),
                        Value.Raw(Value.Type.Number, 2.0),
                        Value.Raw(Value.Type.Number, 3.0)
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfDouble() {
        Assert.assertEquals(
            Value(listOf(1.0, 2.0, 3.0)),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(Value.Type.Number, 1.0),
                        Value.Raw(Value.Type.Number, 2.0),
                        Value.Raw(Value.Type.Number, 3.0)
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfBoolean() {
        Assert.assertEquals(
            Value(listOf(true, false, true)),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(Value.Type.Boolean, true),
                        Value.Raw(Value.Type.Boolean, false),
                        Value.Raw(Value.Type.Boolean, true)
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfString() {
        Assert.assertEquals(
            Value(listOf("A", "B", "C")),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(Value.Type.String, "A"),
                        Value.Raw(Value.Type.String, "B"),
                        Value.Raw(Value.Type.String, "C")
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfObject() {
        Assert.assertEquals(
            Value(listOf(
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret"),
                Auth("id", "name", "secret")
            )),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(
                            Value.Type.Object, listOf(
                                Value.Raw(Value.Type.String, "id"),
                                Value.Raw(Value.Type.String, "name"),
                                Value.Raw(Value.Type.String, "secret")
                            )
                        ),
                        Value.Raw(
                            Value.Type.Object, listOf(
                                Value.Raw(Value.Type.String, "id"),
                                Value.Raw(Value.Type.String, "name"),
                                Value.Raw(Value.Type.String, "secret")
                            )
                        ),
                        Value.Raw(
                            Value.Type.Object, listOf(
                                Value.Raw(Value.Type.String, "id"),
                                Value.Raw(Value.Type.String, "name"),
                                Value.Raw(Value.Type.String, "secret")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfArrayOfInt() {
        Assert.assertEquals(
            Value(listOf(
                listOf(1, 2, 3),
                listOf(1, 2, 3),
                listOf(1, 2, 3)
            )),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(
                            Value.Type.Array,
                            listOf(Value.Raw(
                                Value.Type.Number, 1L),
                                Value.Raw(Value.Type.Number, 2L),
                                Value.Raw(Value.Type.Number, 3L)
                            )
                        ),
                        Value.Raw(
                            Value.Type.Array,
                            listOf(
                                Value.Raw(Value.Type.Number, 1L),
                                Value.Raw(Value.Type.Number, 2L),
                                Value.Raw(Value.Type.Number, 3L)
                            )
                        ),
                        Value.Raw(
                            Value.Type.Array,
                            listOf(Value.Raw(Value.Type.Number, 1L),
                                Value.Raw(Value.Type.Number, 2L),
                                Value.Raw(Value.Type.Number, 3L)
                            )
                        ),
                    )
                )
            )
        )
    }

    @Test
    fun constructArrayOfArrayOfArrayOfInt() {
        Assert.assertEquals(
            Value(listOf(
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
            )),
            Value(
                Value.Raw(
                    Value.Type.Array,
                    listOf(
                        Value.Raw(Value.Type.Array, listOf(
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L)))
                        )),
                        Value.Raw(Value.Type.Array, listOf(
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L)))
                        )),
                        Value.Raw(Value.Type.Array, listOf(
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L))),
                            Value.Raw(Value.Type.Array,
                                listOf(Value.Raw(Value.Type.Number, 1L),
                                    Value.Raw(Value.Type.Number, 2L),
                                    Value.Raw(Value.Type.Number, 3L)))
                        )),
                    )
                )
            )
        )
    }
}