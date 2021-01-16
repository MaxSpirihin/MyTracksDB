package com.max.spirihin.mytracksdb.utilities

object Utils {
    fun distanceToString(distanceInMeters : Int) : String {
        val distanceInKM = distanceInMeters / 1000.0
        return "%.2f".format(distanceInKM) + " km"
    }

    fun distanceToStringShort(distanceInMeters : Int) : String {
        val distanceInKM = distanceInMeters / 1000.0
        return "%.1f".format(distanceInKM)
    }

    fun secondsToString(seconds : Int) : String {
        val h = seconds / 3600
        val m = (seconds / 60) % 60
        val s = seconds % 60
        return "${String.format("%02d",h)}:${String.format("%02d",m)}:${String.format("%02d",s)}"
    }

    fun paceToString(seconds : Int) : String {
        val m = (seconds / 60) % 60
        val s = seconds % 60
        return "${String.format("%02d",m)}'${String.format("%02d",s)}\" /km"
    }
}