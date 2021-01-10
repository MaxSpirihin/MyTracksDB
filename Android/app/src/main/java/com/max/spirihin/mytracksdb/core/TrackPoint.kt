package com.max.spirihin.mytracksdb.core

import android.location.Location
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class TrackPoint(val time: Date, val latitude: Double, val longitude: Double, val accuracy: Double, val steps: Int, val heartrate: Int) {

    val timeInSeconds : Long
        get() = time.time / 1000

    companion object {
        fun distanceBetween(point1: TrackPoint, point2: TrackPoint) : Double {
            val result = FloatArray(1)
            Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, result)
            return result[0].toDouble()
        }
    }
}