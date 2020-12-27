package com.max.spirihin.mytracksdb.core.db

import androidx.room.TypeConverter
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.Date

class Converters {
    @TypeConverter
    fun dateFromLong(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToLong(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun exerciseTypeFromString(value: String): ExerciseType {
        return try {
            ExerciseType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            return ExerciseType.UNKNOWN
        }
    }

    @TypeConverter
    fun dateToLong(exerciseType: ExerciseType): String {
        return exerciseType.toString()
    }

}