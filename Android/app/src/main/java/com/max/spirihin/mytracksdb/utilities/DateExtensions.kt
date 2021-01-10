package com.max.spirihin.mytracksdb.utilities

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

fun Date.toShortString(): String {
    return this.toStringFormat("dd.MM - hh:mm:ss")
}

fun Date.toStringFormat(format: String): String {
    val dateFormat: DateFormat = SimpleDateFormat(format)
    return dateFormat.format(this)
}