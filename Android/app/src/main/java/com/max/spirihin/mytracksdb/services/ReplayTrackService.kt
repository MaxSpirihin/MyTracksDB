package com.max.spirihin.mytracksdb.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.core.TrackPoint
import com.max.spirihin.mytracksdb.listeners.StepCounterListener
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.listeners.HeartRateListener
import com.max.spirihin.mytracksdb.listeners.LocationListener
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*
import kotlin.concurrent.schedule

class ReplayTrackService : TrackPointsProviderService() {

    private var mOnChangeObservers = mutableListOf<(TrackPoint) -> Unit>()
    private var mAllPoints: MutableList<TrackPoint> = mutableListOf()
    private var mPointIndex: Int = -1

    override fun onCreate() {
        super.onCreate()

        Print.Log("[ReplayTrackService] onCreate")
        TrackRecordManager.attachPointsProvider(this)
        val track = TracksDatabase.loadAllTracks().sortedByDescending { _T -> _T.date }.first().getTrack()!!
        for (segment in track.segments) {
            mAllPoints.addAll(segment.points)
        }

        Timer().schedule(0, 100) {
            moveToNext()
        }
    }

    override fun getCurrentPoint() : TrackPoint? {
        return mAllPoints[mPointIndex]
    }

    override fun subscribe(observer: (TrackPoint) -> Unit) {
        mOnChangeObservers.add(observer)
    }

    override fun unsubscribe(observer: (TrackPoint) -> Unit) {
        if (mOnChangeObservers.contains(observer))
            mOnChangeObservers.remove(observer)
    }

    override fun destroy(context: Context) {
        context.stopService(Intent(context, ReplayTrackService::class.java))
    }

    fun moveToNext() {
        if (mPointIndex < mAllPoints.size - 1)
            mPointIndex++

        val point = getCurrentPoint() ?: return
        for (observer in mOnChangeObservers) {
            observer?.invoke(point)
        }
    }

    override fun onDestroy() {
        Print.Log("[ReplayTrackService] onDestroy")

        mOnChangeObservers.clear()

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}