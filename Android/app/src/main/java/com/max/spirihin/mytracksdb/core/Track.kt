package com.max.spirihin.mytracksdb.core

import com.max.spirihin.mytracksdb.utilities.Utils
import java.util.*
import com.max.spirihin.mytracksdb.utilities.toShortString
import kotlin.collections.ArrayList

class Track (val exerciseType : ExerciseType) {

    //region attrubutes
    val segments: ArrayList<Segment> = ArrayList()
    var id: Long = 0

    private var needNewSegment = false
    //endregion

    //region properties
    val distance: Int
        get() = segments.sumBy { s -> s.distance }

    val date: Date
        get() = if (segments.size < 1) Date() else segments[0].startTime

    /* full duration in seconds */
    val duration: Int
        get() = segments.sumBy { s -> s.duration }

    /* pace in seconds */
    val pace: Int
        get() = if (distance > 0) duration * 1000 / distance else 0

    val totalSteps: Int
        get() = segments.sumBy { s -> s.totalSteps }

    val cadence: Int
        get() = if (duration > 0) totalSteps * 60 / duration else 0

    val averageHeartrate : Int
        get() {
            var totalHeartrateXDuration = 0
            var totalDuration = 0
            for (sector in segments) {
                if (sector.averageHeartrate > 0) {
                    totalHeartrateXDuration += sector.averageHeartrate * sector.duration
                    totalDuration += sector.duration
                }
            }
            return if (totalDuration > 0) totalHeartrateXDuration / totalDuration else 0
        }

    val maxHeartrate : Int
        get() = if (segments.isNotEmpty()) segments.map { p -> p.maxHeartrate }.max() ?: 0 else 0

    val currentHeartrate : Int
        get() = if (segments.isNotEmpty()) segments.last().currentHeartrate else 0

    val dateStr: String
        get() = date.toShortString()

    val speechDistance : Int
        get() = if (exerciseType == ExerciseType.EASY_RUN) 500 else Int.MAX_VALUE

    val speechStr: String
        get() {
            return "Pass $distance meters." + if (averageHeartrate > 0) "Average heartrate is $averageHeartrate" else ""
        }

    val infoStr: String
        get() {
            var str = "type = ${exerciseType}\n" +
                    "date = $dateStr\n" +
                    "time = ${Utils.secondsToString(duration)}\n" +
                    "distance = $distance\n" +
                    "pace = ${Utils.secondsToString(pace)}\n" +
                    "sectors = ${segments.size}\n" +
                    "points = ${segments.sumBy { s -> s.points.size }}\n" +
                    "total steps = $totalSteps\n" +
                    "cadence = $cadence\n" +
                    "average heartrate = $averageHeartrate\n" +
                    "max heartrate = $maxHeartrate\n" +
                    "current heartrate = $currentHeartrate\n"

            val errPoints = mutableListOf<TrackPoint>()
            for (segment in segments) {
                for (point in segment.points) {
                    if (point.accuracy > 50) {
                        errPoints.add(point)
                    }
                }
            }

            if (errPoints.size > 0) {
                str += "errors "
                for (point in errPoints) {
                    str += String.format("%02f", point.accuracy) + " "
                }
            }

            return str
        }
    //endregion

    //region public methods
    fun newSegment() {
        needNewSegment = true
    }

    fun addPoint(point: TrackPoint) {
        if (segments.size == 0 || needNewSegment)
            segments.add(Segment())

        needNewSegment = false

        segments.last().points.add(point)
    }

    //endregion
}