package com.max.spirihin.mytracksdb.core

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.room.Room
import com.max.spirihin.mytracksdb.core.db.TrackDB
import com.max.spirihin.mytracksdb.core.db.TrackDao
import com.max.spirihin.mytracksdb.core.db.TracksRoomDatabase
import com.max.spirihin.mytracksdb.utilities.Print
import com.max.spirihin.mytracksdb.utilities.RoomBackup
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.math.abs

object TracksDatabase {

    private const val DATABASE_NAME = "tracks_database"
    private const val FOR_RESTORE_FILE_NAME = "for_restore"

    private var mDao : TrackDao? = null
    private var mContext : Context? = null
    private var mRoomDB : TracksRoomDatabase? = null
    private val gpxPath: String = Environment.getExternalStorageDirectory().absolutePath + "/S Health/GPX"

    fun init(context: Context) {
        mContext = context
        mRoomDB = Room
                .databaseBuilder(
                        context,
                        TracksRoomDatabase::class.java,
                        DATABASE_NAME
                )
                .addMigrations(*TracksRoomDatabase.MIGRATIONS)
                .allowMainThreadQueries()
                .build()
        mDao = mRoomDB!!.dao()
    }

    fun updateDatabase() {
        val dao = mDao ?: return

        for (trackDB in dao.getAll().toList()) {
            val track = trackDB.getTrack();
            if (track != null) {
                dao.update(TrackDB.create(track))
            }
            else {
                dao.deleteById(trackDB.id)
            }
        }
    }

    fun backupSqlFile(verbose : Boolean = false) {
        RoomBackup()
                .context(mContext!!)
                .database(mRoomDB!!)
                .useExternalStorage(true)
                .apply {
                    onCompleteListener { success, message ->
                        if (verbose)
                            return@onCompleteListener

                        val msg = "Backup Database. Success: $success, Message: $message"
                        Print.Log(msg)
                        Toast.makeText(mContext!!, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                    }
                }
                .backup()

        //reload db
        init(mContext!!)
    }

    fun restoreSqlFile() {
        //make extra backup before restore
        backupSqlFile(true)

        RoomBackup()
                .context(mContext!!)
                .database(mRoomDB!!)
                .useExternalStorage(true)
                .apply {
                    onCompleteListener { success, message ->
                        val msg = "Restore Database. Success: $success, Message: $message"
                        Print.Log(msg)
                        Toast.makeText(mContext!!, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                    }
                }.restore(FOR_RESTORE_FILE_NAME)

        //reload db
        init(mContext!!)
    }

    fun saveTrack(track: Track) : Long {
       return mDao?.insert(TrackDB.create(track)) ?: 0
    }

    fun deleteTrack(track: Track) {
        mDao?.deleteById(track.id)
    }

    fun loadTrackByID(id: Long) : Track? {
        return mDao?.loadById(id)?.getTrack()
    }

    fun loadAllTracks() : List<TrackDB> {
        return mDao?.getAll()!!
    }

    fun tryLoadGPXForTrack(track: Track) : Pair<Track?, HashMap<String, String>> {
        if (!File(gpxPath).exists())
            return Pair(null, HashMap())

        val files = File(gpxPath).listFiles()
        for (file in files) {
            try {
                val dateFromFileName = SimpleDateFormat("yyyyMMdd_hhmmss").parse(file.nameWithoutExtension)
                if (abs(track.date.time - dateFromFileName.time) < 60 * 1000) {
                    return TrackParser.fromGPX(file)
                }
            } catch (e : ParseException) {
                //ignored
            }
        }

        return Pair(null, HashMap())
    }
}