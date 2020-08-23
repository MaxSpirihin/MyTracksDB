package com.max.spirihin.mytracksdb

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
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
    private var isRecording = false
    private var isInited = false
    //endregion

    //region public methods
    fun init(activity: Activity) {
        if (isInited) {
            Toast.makeText(activity.applicationContext, "TrackRecordManager already inited", Toast.LENGTH_LONG).show()
            return
        }
        isInited = true
    }

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun startRecording(activity: Activity) {
        if (isRecording) throw Exception("Record is already running. Please call StopRecording")
        isRecording = true
        Log.d("MyLogs", "Start record track")
        track = Track()

        //TODO: check if service is already running
        activity.startService(Intent(activity, LocationService::class.java))
    }

    fun addTrackPoint(location: Location?) {
        if (!isInited || !isRecording) return
        if (location == null || track == null) return
        track!!.addPoint(location)
        for (listener in mListeners) {
            listener.onReceive(track!!)
        }
    }

    fun stopRecording(activity: Activity) {
        if (!isRecording) return
        isRecording = false
        activity.stopService(Intent(activity, LocationService::class.java))
    }

    fun registerListener(listener: ITrackRecordListener) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: ITrackRecordListener?) {
        mListeners.remove(listener)
    }
    //endregion
}