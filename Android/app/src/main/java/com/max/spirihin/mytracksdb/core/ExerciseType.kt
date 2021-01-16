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

fun ExerciseType.getMenuMainIconId(): Int {
    return when (this) {
        ExerciseType.EASY_RUN -> R.drawable.menu_main_running
        ExerciseType.CONTROL_RUN -> R.drawable.menu_main_control_run
        ExerciseType.WALKING -> R.drawable.menu_main_walking
        ExerciseType.BICYCLE -> R.drawable.menu_main_bicycle
        ExerciseType.SKIING -> R.drawable.menu_main_skiing
        ExerciseType.SKATES -> R.drawable.menu_main_skates
        else -> 0
    }
}

fun ExerciseType.getName(): String {
    return when (this) {
        ExerciseType.EASY_RUN -> "Easy run"
        ExerciseType.CONTROL_RUN -> "Control run"
        ExerciseType.WALKING -> "Walking"
        ExerciseType.BICYCLE -> "Bicycle"
        ExerciseType.SKIING -> "Skiing"
        ExerciseType.SKATES -> "Skates"
        ExerciseType.UNKNOWN -> "All"
        else -> ""
    }
}

