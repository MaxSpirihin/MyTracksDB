package com.max.spirihin.mytracksdb.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.max.spirihin.mytracksdb.core.TrackPoint
import com.max.spirihin.mytracksdb.listeners.StepCounterListener
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.listeners.HeartRateListener
import com.max.spirihin.mytracksdb.listeners.LocationListener
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

class RecordTrackService : TrackPointsProviderService() {

    private var stepCounterListener : StepCounterListener? = null
    private var locationListener : LocationListener? = null
    private var heartRateListener : HeartRateListener? = null

    private var mOnChangeObservers = mutableListOf<(TrackPoint) -> Unit>()



    override fun onCreate() {
        super.onCreate()

        Print.Log("[RecordTrackService] onCreate")

        stepCounterListener = StepCounterListener()
        heartRateListener = HeartRateListener()
        locationListener = LocationListener { location -> onLocationChanged() }
        TrackRecordManager.attachPointsProvider(this)
    }

    override fun getCurrentPoint() : TrackPoint? {
        val location = locationListener?.lastLocation ?: return null
        return TrackPoint(
                Calendar.getInstance().time,
                location.latitude,
                location.longitude,
                location.accuracy.toDouble(),
                stepCounterListener?.stepsCount ?: 0,
                heartRateListener?.currentHeartrate ?: 0
        )
    }

    override fun subscribe(observer: (TrackPoint) -> Unit) {
        mOnChangeObservers.add(observer)
    }

    override fun unsubscribe(observer: (TrackPoint) -> Unit) {
        if (mOnChangeObservers.contains(observer))
            mOnChangeObservers.remove(observer)
    }

    override fun destroy(context: Context) {
        context.stopService(Intent(context, RecordTrackService::class.java))
    }

    @Suppress("DEPRECATION")
    fun onLocationChanged() {
        val point = getCurrentPoint() ?: return
        for (observer in mOnChangeObservers) {
            observer?.invoke(point)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Print.Log("[RecordTrackService] onStartCommand")

        locationListener?.startListen(this)
        stepCounterListener?.startListen(this)
        heartRateListener?.startListen(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Print.Log("[RecordTrackService] onDestroy")

        locationListener?.stopListen()
        stepCounterListener?.stopListen()
        heartRateListener?.stopListen()

        mOnChangeObservers.clear()

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}