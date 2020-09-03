package com.max.spirihin.mytracksdb

import android.os.Environment
import java.io.File
import java.sql.Date
import java.text.ParseException
import java.text.SimpleDateFormat

object TracksDatabase {

    private var idCounter: Int? = null
    private val directoryPath: String = Environment.getExternalStorageDirectory().absolutePath + "/MyTracksDB"
    private val gpxPath: String = Environment.getExternalStorageDirectory().absolutePath + "/S Health/GPX"

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

    fun deleteTrack(track: Track) {
        trackFileInStorage(track.id).delete()
    }

    fun loadTrackByID(id: Int) : Track? {
        val file = trackFileInStorage(id)
        if (!file.exists())
            return null

        return Track.fromJSON(file.nameWithoutExtension.toInt(), file.readText())
    }

    fun loadAllTracks() : ArrayList<Track> {

        val directory = File(directoryPath)
        if (!directory.exists())
            directory.mkdir()

        val result = arrayListOf<Track>()
        File(directoryPath).listFiles().forEach { file ->
            val track = Track.fromJSON(file.nameWithoutExtension.toInt(), file.readText())
            if (track != null) {
                result.add(track)
            }
        }

        return result
    }

    fun tryLoadGPXForTrack(track: Track) : Pair<Track?, HashMap<String, String>> {
        if (!File(gpxPath).exists())
            return Pair(null, HashMap())

        var files = File(gpxPath).listFiles()
        for (file in files) {
            try {
                val dateFromFileName = SimpleDateFormat("yyyyMMdd_hhmmss").parse(file.nameWithoutExtension)
                if (Math.abs(track.points.first().time.time - dateFromFileName.time) < 60 * 1000) {
                    return Track.fromGPX(file)
                }
            } catch (e : ParseException) {
                //ignored
            }
        }

        return Pair(null, HashMap())
    }

}