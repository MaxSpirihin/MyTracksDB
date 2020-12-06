package com.max.spirihin.mytracksdb.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.max.spirihin.mytracksdb.Helpers.TextToSpeechHelper
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.activities.RecordTrackActivity
import com.max.spirihin.mytracksdb.core.TrackPoint
import com.max.spirihin.mytracksdb.listeners.StepCounterListener
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.listeners.HeartRateListener
import com.max.spirihin.mytracksdb.listeners.LocationListener
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

class RecordTrackService : Service() {

    private var textToSpeech: TextToSpeechHelper? = null
    private var distanceForSpeech : Int = 0
    private var stepCounterListener : StepCounterListener? = null
    private var locationListener : LocationListener? = null
    private var heartRateListener : HeartRateListener? = null

    private var notification: Notification? = null

    companion object {
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "RecordTrackService"
        const val SPEECH_UPDATE_DISTANCE = 500
    }

    override fun onCreate() {
        super.onCreate()

        Print.Log("[RecordTrackService] onCreate")

        textToSpeech = TextToSpeechHelper(applicationContext)
        distanceForSpeech = SPEECH_UPDATE_DISTANCE
        stepCounterListener = StepCounterListener()
        heartRateListener = HeartRateListener()
        locationListener = LocationListener { location -> onLocationChanged(location) }
        TrackRecordManager.attachService(this)
    }

    fun getLastPoint() : TrackPoint? {
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

    @Suppress("DEPRECATION")
    fun onLocationChanged(location: Location) {
        val point = getLastPoint() ?: return

        TrackRecordManager.addTrackPoint(point)
        val track = TrackRecordManager.track!!
        updateNotification("Running", "${track.distance}m. | ${track.duration / 60}:${track.duration % 60}")

        if (track.distance > distanceForSpeech) {
            textToSpeech!!.speak(track.speechStr)
            distanceForSpeech += SPEECH_UPDATE_DISTANCE
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Print.Log("[RecordTrackService] onStartCommand")
        createNotificationChannel()
        updateNotification("Running", "")

        locationListener?.startListen(this)
        stepCounterListener?.startListen(this)
        heartRateListener?.startListen(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Print.Log("[RecordTrackService] onDestroy")

        textToSpeech?.destroy()
        locationListener?.stopListen()
        stepCounterListener?.stopListen()
        heartRateListener?.stopListen()

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