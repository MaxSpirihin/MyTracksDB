package com.max.spirihin.mytracksdb

import android.os.Environment
import java.io.File

object TracksDatabase {

    private var idCounter: Int? = null
    private val directoryPath: String = Environment.getExternalStorageDirectory().absolutePath + "/MyTracksDB"

    private fun getNextId() : Int {
        if (idCounter == null) {
            //TODO i know this is shit i will fix it later :)
            val tracksInDB = loadAllTracks()
            idCounter = if (tracksInDB.isEmpty()) 0 else tracksInDB.maxBy { t -> t.id }!!.id
        }

        idCounter = idCounter!! + 1
        return idCounter!!
    }

    private fun trackFileInStorage(trackId: Int) : File {
        val directory = File(directoryPath)
        if (!directory.exists())
            directory.mkdir()

        return File(directoryPath, "$trackId.txt")
    }

    fun saveTrack(track: Track) : Int {
        track.id = getNextId();
        trackFileInStorage(track.id).writeText(track.toJSON())
        return track.id
    }

    fun loadTrackByID(id: Int) : Track? {
        val file = trackFileInStorage(id)
        if (!file.exists())
            return null

        return Track.fromJSON(file.readText())
    }

    fun loadAllTracks() : ArrayList<Track> {

        val directory = File(directoryPath)
        if (!directory.exists())
            directory.mkdir()

        val result = arrayListOf<Track>()
        File(directoryPath).listFiles().forEach { file ->
            val track = Track.fromJSON(file.readText())
            if (track != null) {
                track.id = file.nameWithoutExtension.toInt()
                result.add(track)
            }
        }

        return result
    }

    fun tryLoadGPXForTrack(track: Track) : Track? {
        throw NotImplementedError("aaa")
    }

}