package com.codedillo.wifiutils.utility

internal fun String.quoted(): String {
    return if (startsWith("\"") && endsWith("\"")) this else "\"$this\""
}