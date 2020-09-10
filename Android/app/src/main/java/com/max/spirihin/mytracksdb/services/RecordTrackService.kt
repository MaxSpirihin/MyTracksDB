package com.max.spirihin.mytracksdb.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.activities.RecordTrackActivity
import com.max.spirihin.mytracksdb.listeners.StepCounterListener
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.listeners.LocationListener
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

class RecordTrackService : Service() {

    private var textToSpeech: TextToSpeech? = null//todo move to separate class
    private var distanceForSpeech : Int = 0
    private var stepCounterListener : StepCounterListener? = null
    private var locationListener : LocationListener? = null

    private var notification: Notification? = null

    companion object {
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "RecordTrackService"
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
        stepCounterListener = StepCounterListener()
        locationListener = LocationListener { location -> onLocationChanged(location) }
    }

    @Suppress("DEPRECATION")
    fun onLocationChanged(location: Location) {
        TrackRecordManager.addTrackPoint(location, stepCounterListener?.stepsCount ?: 0)
        val track = TrackRecordManager.track!!
        updateNotification("Running", "${track.distance}m. | ${track.duration / 60}:${track.duration % 60}")

        if (TrackRecordManager.track!!.distance > distanceForSpeech) {
            textToSpeech!!.speak(TrackRecordManager.track!!.speechStr, TextToSpeech.QUEUE_FLUSH, null)
            distanceForSpeech += SPEECH_UPDATE_DISTANCE
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Print.Log("[RecordTrackService] onStartCommand")
        createNotificationChannel()
        updateNotification("Running", "")

        locationListener?.startListen(this)
        stepCounterListener?.startListen(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Print.Log("[RecordTrackService] onDestroy")

        textToSpeech?.stop()
        textToSpeech?.shutdown()

        locationListener?.stopListen()
        stepCounterListener?.stopListen()

        super.onDestroy()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "RecordTrackService",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}