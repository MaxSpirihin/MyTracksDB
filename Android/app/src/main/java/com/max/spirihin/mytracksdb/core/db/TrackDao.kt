package com.max.spirihin.mytracksdb.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): List<Track>

    @Query("SELECT * FROM track WHERE id = (:trackId)")
    fun loadById(trackId: Long): Track

    @Insert
    fun insert(track: Track) : Long

    @Update
    fun update(track: Track)

    @Query("DELETE FROM track WHERE id = :trackId")
    fun deleteById(trackId: Long)
}