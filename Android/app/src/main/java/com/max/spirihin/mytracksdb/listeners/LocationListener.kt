package com.max.spirihin.mytracksdb.listeners

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.utilities.Print

class LocationListener(private val callback : ((Location) -> Unit)?) : android.location.LocationListener, IListener {

    private var locationManager : LocationManager? = null
    var lastLocation : Location? = null
        private set

    @Suppress("DEPRECATION")
    override fun onLocationChanged(location: Location) {
        Print.Log("[LocationListener] onLocationChanged ${location.longitude} ${location.latitude} ${location.accuracy}")
        lastLocation = location
        callback?.invoke(location)
    }

    @SuppressLint("MissingPermission")
    override fun startListen(context: Context) {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = true
        criteria.isBearingRequired = false
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
                1000 * Preferences.gpsUpdateSeconds.toLong(),
                Preferences.gpsUpdateMeters.toFloat(),
                criteria,
                this,
                null
        )
    }

    override fun stopListen() {
        locationManager?.removeUpdates(this)
    }

}