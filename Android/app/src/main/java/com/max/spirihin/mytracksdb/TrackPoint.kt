package com.max.spirihin.mytracksdb

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TrackPoint(val time: Date, val latitude: Double, val longitude: Double, val accuracy: Double) {

    override fun toString(): String {
        val dateFormat: DateFormat = SimpleDateFormat("hh:mm:ss")
        return dateFormat.format(time) + " - " + latitude + " " + longitude + " " + accuracy
    }
}