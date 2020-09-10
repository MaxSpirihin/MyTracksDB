package com.max.spirihin.mytracksdb.core

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TrackPoint(val time: Date, val latitude: Double, val longitude: Double, val accuracy: Double, val steps: Int) {

    override fun toString(): String {
        val dateFormat: DateFormat = SimpleDateFormat("hh:mm:ss")
        return dateFormat.format(time) + " - " + latitude + " " + longitude + " " + accuracy + " " + steps
    }
}