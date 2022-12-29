package com.codedillo.numberpicker

import android.content.res.Resources
import android.view.View

internal val Int.dp: Float get() = this * Resources.getSystem().displayMetrics.density
internal val Int.dip: Int get() = this.dp.toInt()

internal val Double.dp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.density
internal val Double.dip: Int get() = this.dp.toInt()

internal fun View.setPaddingVertical(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}

internal data class Color(
    var red: Int,
    var green: Int,
    var blue: Int,
    var alpha: Int = 0xFF
) {

    val argb: Int
        get() {
            return ((alpha shl 24) and 0xFF000000.toInt()) or ((red shl 16) and 0x00FF0000) or ((green shl 8) and 0x0000FF00) or (blue and 0x000000FF)
        }

    constructor(color: Int) : this(
        android.graphics.Color.red(color),
        android.graphics.Color.green(color),
        android.graphics.Color.blue(color),
        android.graphics.Color.alpha(color)
    )

    operator fun minus(other: Color): Color {
        return Color(red - other.red, green - other.green, blue - other.blue)
    }

    operator fun plus(other: Color): Color {
        return Color(red + other.red, green + other.green, blue + other.blue)
    }

    operator fun times(value: Float): Color {
        return Color((red * value).toInt(), (green * value).toInt(), (blue * value).toInt())
    }

    companion object {
        const val WHITE = android.graphics.Color.WHITE
        const val BLACK = android.graphics.Color.BLACK
    }
}
