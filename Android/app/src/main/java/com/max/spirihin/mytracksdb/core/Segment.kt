package com.max.spirihin.mytracksdb.core

import android.location.Location
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*
import kotlin.math.max

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
        get() = if (points.size < 2) 0 else (points.last().timeInSeconds - points.first().timeInSeconds).toInt()

    /* full duration in seconds in case if recording is in process */
    val durationForProcessing: Int
        get() {
            return if (points.size < 1) 0 else (Calendar.getInstance().time.time / 1000 - points.first().timeInSeconds).toInt()
        }

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

    val averageAltitude : Double
        get() =  if (points.isNotEmpty()) points.sumByDouble { p -> p.altitude } / points.count() else 0.0

    val averageSpeedNative : Double
        get() =  if (points.isNotEmpty()) points.sumByDouble { p -> p.speed } / points.count() else 0.0

    val currentHeartrate : Int
        get() = if (points.isNotEmpty()) points.last().heartrate else 0

    val maxHeartrate : Int
        get() = if (points.isNotEmpty()) points.map { p -> p.heartrate }.max() ?: 0 else 0

    //in m/s
    val maxSpeed : Double
    get() {
        var maxSpeed = 0.0
        for (i in 1 until points.size) {
            maxSpeed = max(getSpeedAtPoint(i), maxSpeed)
        }
        return maxSpeed
    }

    fun getSpeedAtPoint(index: Int) : Double {
        var distance = 0.0
        val startIndex = max(0, index - 15)
        for (i in startIndex until index) {
            distance += TrackPoint.distanceBetween(points[i], points[i+1])
        }
        return distance / (points[index].timeInSeconds - points[startIndex].timeInSeconds)
    }

    /*
    start, end - in meters
     */
    fun getSpeedAtRange(start: Int, end: Int) : Double {
        var distance = 0.0
        var startDistance : Double? = null
        var startTime : Long? = null
        var endTime : Long? = null
        for (i in 0 until points.size - 1) {
            distance += TrackPoint.distanceBetween(points[i], points[i+1])
            if (startDistance == null && distance >= start) {
                startDistance = distance
                startTime = points[i].timeInSeconds
            }

            if (distance > end) {
                endTime = points[i].timeInSeconds
                break
            }
        }

        endTime = endTime ?: points.last().timeInSeconds

        if (startDistance == null || startTime == null)
            return 0.0

        return (distance - startDistance) / (endTime - startTime)
    }

    fun getHeartrateAtRange(start: Int, end: Int) : Int {
        var distance = 0.0
        var startDistance : Double? = null
        var heartrateSum = 0
        var pointsCount = 0
        for (i in 0 until points.size - 1) {
            distance += TrackPoint.distanceBetween(points[i], points[i+1])
            if (startDistance == null && distance >= start) {
                startDistance = distance
            }

            if (distance >= start) {
                heartrateSum += points[i].heartrate
                pointsCount++
            }

            if (distance > end) {
                break
            }
        }

        if (pointsCount == 0)
            return 0

        return heartrateSum / pointsCount
    }

    val fastestPace : Int
        get() {
            val maxSpeed = maxSpeed
            return if (maxSpeed > 0) (1000.0 / maxSpeed).toInt() else 0
        }

    //endregion
}