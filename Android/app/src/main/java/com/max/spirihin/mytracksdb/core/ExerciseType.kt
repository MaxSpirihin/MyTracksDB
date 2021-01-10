package com.max.spirihin.mytracksdb.core

import com.max.spirihin.mytracksdb.R

enum class ExerciseType {
    UNKNOWN,
    EASY_RUN,
    CONTROL_RUN,
    WALKING,
    BICYCLE,
    SKATES,
    SKIING
}

fun ExerciseType.getIconId(): Int {
   return when (this) {
        ExerciseType.EASY_RUN -> R.drawable.icon_running
        ExerciseType.WALKING -> R.drawable.icon_walking
        ExerciseType.BICYCLE -> R.drawable.icon_bicycle
        ExerciseType.SKIING -> R.drawable.icon_skiing
        ExerciseType.SKATES -> R.drawable.icon_skates
        else -> 0
    }
}

