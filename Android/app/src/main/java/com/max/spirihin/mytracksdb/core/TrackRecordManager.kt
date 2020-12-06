package com.max.spirihin.mytracksdb.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.max.spirihin.mytracksdb.services.RecordTrackService
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

object TrackRecordManager {
    //region nested types
    interface ITrackRecordListener {
        fun onReceive(track: Track)
    }
    //endregion

    //properties
    private var mListeners = HashSet<ITrackRecordListener>()
    private var mService : RecordTrackService? = null

    var track: Track? = null
        private set
    var isRecording = false
        private set
    var paused = false
        private set

    //endregion

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun startRecording(activity: Activity) {
        if (isRecording) throw Exception("Record is already running. Please call StopRecording")
        isRecording = true
        Print.Log("Start record track")
        track = Track()

        activity.startService(Intent(activity, RecordTrackService::class.java))
    }

    fun attachService(service : RecordTrackService){
        mService = service
    }

    fun pauseRecording(pause : Boolean) {
        if (!isRecording || pause == paused)
            return

        paused = pause

        val lastPoint = mService!!.getLastPoint()
        if (lastPoint != null)
            track?.addPoint(lastPoint)

        if (pause)
            track?.newSegment()
    }

    fun addTrackPoint(point : TrackPoint) {
        if (!isRecording || paused) return
        if (track == null) return
        track!!.addPoint(point)
        for (listener in mListeners) {
            listener.onReceive(track!!)
        }
    }

    fun stopRecording(activity: Activity) {
        if (!isRecording) return
        isRecording = false
        mService = null
        activity.stopService(Intent(activity, RecordTrackService::class.java))
    }

    fun registerListener(listener: ITrackRecordListener) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: ITrackRecordListener?) {
        mListeners.remove(listener)
    }
    //endregion
}