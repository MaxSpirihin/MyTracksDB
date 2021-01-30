package com.max.spirihin.mytracksdb.Helpers

import com.max.spirihin.mytracksdb.core.ExerciseType

enum class SpeakerInfoType {
    TOTAL_DISTANCE,
    TOTAL_TIME,
    AVERAGE_PACE,
    CURRENT_PACE,
    AVERAGE_HEARTRATE,
    CURRENT_HEARTRATE;

    fun getDefaultValue(exerciseType: ExerciseType): Boolean {
        return when (this) {
            TOTAL_DISTANCE -> true
            AVERAGE_PACE -> true
            CURRENT_HEARTRATE -> true
            else -> false
        }
    }

    fun getString(): String {
        return when (this) {
            TOTAL_DISTANCE -> "Total distance"
            TOTAL_TIME -> "Total time"
            AVERAGE_PACE -> "Average pace"
            CURRENT_PACE -> "Current pace"
            AVERAGE_HEARTRATE -> "Average heartrate"
            CURRENT_HEARTRATE -> "Current heartrate"
        }
    }
}