package com.max.spirihin.mytracksdb.utilities

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

fun Date.toShortString(): String {
    val dateFormat: DateFormat = SimpleDateFormat("dd.MM - hh:mm:ss")
    return dateFormat.format(this)
}