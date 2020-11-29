package com.max.spirihin.mytracksdb.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import com.max.spirihin.mytracksdb.services.RecordTrackService
import com.max.spirihin.mytracksdb.utilities.Print
import com.wahoofitness.connector.capabilities.Heartrate
import java.util.*

object TrackRecordManager {
    //region nested types
    interface ITrackRecordListener {
        fun onReceive(track: Track)
    }
    //endregion

    //properties
    var mListeners = HashSet<ITrackRecordListener>()
    var track: Track? = null
        private set
    var isRecording = false
        private set

    //endregion

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun startRecording(activity: Activity) {
        if (isRecording) throw Exception("Record is already running. Please call StopRecording")
        isRecording = true
        Print.Log("Start record track")
        track = Track()

        //TODO: check if service is already running
        activity.startService(Intent(activity, RecordTrackService::class.java))
    }

    fun addTrackPoint(location: Location?, steps: Int, heartrate: Int) {
        if (!isRecording) return
        if (location == null || track == null) return
        track!!.addPoint(location, steps, heartrate)
        for (listener in mListeners) {
            listener.onReceive(track!!)
        }
    }

    fun stopRecording(activity: Activity) {
        if (!isRecording) return
        isRecording = false
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