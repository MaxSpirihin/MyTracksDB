package com.max.spirihin.mytracksdb.Helpers

import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.utilities.Preferences

object SpeakerPreferences {

    fun getSpeakDistance(exerciseType: ExerciseType) : Int {
        val default = when (exerciseType) {
            ExerciseType.EASY_RUN, ExerciseType.SKIING, ExerciseType.BICYCLE -> 1000
            ExerciseType.CONTROL_RUN -> 500
            else -> 0
        }
        return Preferences.getInt("speak_distance_${exerciseType}", default)
    }

    fun setSpeakDistance(exerciseType: ExerciseType, distance: Int) {
        Preferences.setInt("speak_distance_${exerciseType}", distance)
    }

    fun getNeedSpeakType(exerciseType: ExerciseType, speakerInfoType: SpeakerInfoType) : Boolean {
        val default = speakerInfoType.getDefaultValue(exerciseType)
        return Preferences.getBoolean("need_speak_${exerciseType}_${speakerInfoType}", default)
    }

    fun setNeedSpeakType(exerciseType: ExerciseType, speakerInfoType: SpeakerInfoType, value: Boolean) {
        return Preferences.setBoolean("need_speak_${exerciseType}_${speakerInfoType}", value)
    }
}