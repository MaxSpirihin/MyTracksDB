package com.max.spirihin.mytracksdb.core

import android.location.Location
import java.util.*

class Segment {

    //region attrubutes
    val points: ArrayList<TrackPoint> = ArrayList()
    //endregion

    //region properties
    val distance: Int
        get() {
            //TODO we shouldn't compute this all the time, we need cache value and increment in addPoint
            var sum = 0.0
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val result = FloatArray(1)
                Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, result)
                sum += result[0]
            }
            return sum.toInt()
        }

    val startTime: Date
        get() = if (points.size < 1) Date() else points[0].time

    /* full duration in seconds */
    val duration: Int
        get() = if (points.size < 2) 0 else ((points.last().time.time - points.first().time.time) / 1000).toInt()

    val totalSteps: Int
        get() = if (points.isNotEmpty()) points.last().steps else 0

    val averageHeartrate : Int
        get() {
            //TODO this is not quite correct. We should weigh heartrate by duration, not by points count
            var heartrateSum = 0
            var pointsCount = 0
            for (point in points) {
                if (point.heartrate > 0) {
                    heartrateSum += point.heartrate
                    pointsCount++
                }
            }
            return if (pointsCount > 0) heartrateSum / pointsCount else 0
        }

    val currentHeartrate : Int
        get() = if (points.isNotEmpty()) points.last().heartrate else 0

    //endregion
}