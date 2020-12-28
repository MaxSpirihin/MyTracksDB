package com.max.spirihin.mytracksdb.services

import android.app.Service
import android.content.Context
import com.max.spirihin.mytracksdb.core.TrackPoint

abstract class TrackPointsProviderService : Service() {
    abstract fun getCurrentPoint() : TrackPoint?
    abstract fun subscribe(observer : (TrackPoint) -> Unit)
    abstract fun unsubscribe(observer : (TrackPoint) -> Unit)
    abstract fun destroy(context: Context)
}