package com.max.spirihin.mytracksdb.core.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.core.TrackParser
import java.time.Duration
import java.util.*

typealias TrackDB = com.max.spirihin.mytracksdb.core.db.Track

/*
    How to add new fields here
    1. Add Column
    2. pass it to constructor in create method
    3. Increment version number in TracksRoomDatabase
    4. Add migration to array there
    5. In the app go to settings and press update database
 */

@Entity
data class Track(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "data") val data: String?,
        @ColumnInfo(name = "date", defaultValue = "0") val date: Date,
        @ColumnInfo(name = "distance", defaultValue = "0") val distance: Int,
        @ColumnInfo(name = "duration", defaultValue = "0") val duration: Int,
        @ColumnInfo(name = "exercise_type") val exerciseType: ExerciseType,
        @ColumnInfo(name = "pace") val pace: Int
) {
    fun getTrack(): Track? {
        return TrackParser.fromJSON(id, data)
    }

    companion object {
        fun create(track : Track) : TrackDB {
            return TrackDB(track.id, TrackParser.toJSON(track), track.date, track.distance, track.duration, track.exerciseType, track.pace)
        }
    }
}