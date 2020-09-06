package com.max.spirihin.mytracksdb.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.activities.RecordTrackActivity
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

class RecordTrackService : Service(), LocationListener {

    private var textToSpeech: TextToSpeech? = null//todo move to separate class
    var distanceForSpeech : Int = 0
    var notification: Notification? = null

    companion object {
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "LocationService"
        const val SPEECH_UPDATE_DISTANCE = 500
    }

    override fun onCreate() {
        super.onCreate()

        Print.Log("[RecordTrackService] onCreate")

        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsLang = textToSpeech!!.setLanguage(Locale.ENGLISH)
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                        || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(applicationContext, "The Language is not supported!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
            }
        }

        distanceForSpeech = SPEECH_UPDATE_DISTANCE
    }

    override fun onLocationChanged(location: Location) {
        Print.Log("[RecordTrackService] onLocationChanged ${location.longitude} ${location.latitude} ${location.accuracy}")
        TrackRecordManager.addTrackPoint(location)
        val track = TrackRecordManager.track!!
        updateNotification("Running", "${track.distance.toInt()}m. | ${track.duration / 60}:${track.duration % 60}")

        if (TrackRecordManager.track!!.distance > distanceForSpeech) {
            textToSpeech!!.speak(TrackRecordManager.track!!.speechStr, TextToSpeech.QUEUE_FLUSH, null)
            distanceForSpeech += SPEECH_UPDATE_DISTANCE
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Print.Log("[RecordTrackService] onStartCommand")
        updateNotification("Running", "")

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = true
        criteria.isBearingRequired = false
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
                1000 * Preferences.gpsUpdateSeconds.toLong(), Preferences.gpsUpdateMeters.toFloat(), criteria, this, null)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)

        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }

        Print.Log("[RecordTrackService] onDestroy")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun updateNotification(title: String, text: String) {
        val notificationIntent = Intent(this, RecordTrackActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0)
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}