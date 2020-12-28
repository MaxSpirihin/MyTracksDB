package com.max.spirihin.mytracksdb.core

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.max.spirihin.mytracksdb.Helpers.TextToSpeechHelper
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.activities.RecordTrackActivity
import com.max.spirihin.mytracksdb.services.RecordTrackService
import com.max.spirihin.mytracksdb.services.ReplayTrackService
import com.max.spirihin.mytracksdb.services.TrackPointsProviderService
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*

object TrackRecordManager {
    //region nested types
    interface ITrackRecordListener {
        fun onReceive(track: Track)
    }
    //endregion

    const val NOTIFICATION_ID = 10
    const val CHANNEL_ID = "RecordTrackService"

    //properties
    private var mDistanceForSpeech : Int = 0
    private var mListeners = HashSet<ITrackRecordListener>()
    private var mService : TrackPointsProviderService? = null
    private var mTextToSpeech: TextToSpeechHelper? = null
    private var mNotification: Notification? = null

    var track: Track? = null
        private set
    var isRecording = false
        private set
    var paused = false
        private set

    //endregion

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun startRecording(context: Context, exerciseType: ExerciseType) {
        if (isRecording) throw Exception("Record is already running. Please call StopRecording")
        isRecording = true
        Print.Log("Start record track")
        track = Track(exerciseType)
        mTextToSpeech = TextToSpeechHelper(context.applicationContext)
        mDistanceForSpeech = track!!.speechDistance
        context.startService(Intent(context, RecordTrackService::class.java))
        //context.startService(Intent(context, ReplayTrackService::class.java))
    }

    fun attachPointsProvider(service : TrackPointsProviderService){
        if (mService != null)
            throw java.lang.Exception("TrackRecordManager already has points provider")
        mService = service
        mService?.subscribe(::onCurrentPointChanged)
        createNotificationChannel()
        updateNotification("Running", "")
    }

    fun getLastPoint() : TrackPoint? {
        return mService?.getCurrentPoint()
    }

    fun pauseRecording(pause : Boolean) {
        if (!isRecording || pause == paused)
            return

        paused = pause

        val lastPoint = mService!!.getCurrentPoint()
        if (lastPoint != null)
            track?.addPoint(lastPoint)

        if (pause)
            track?.newSegment()
    }

    private fun onCurrentPointChanged(point : TrackPoint) {
        if (!isRecording || paused) return
        if (track == null) return
        track!!.addPoint(point)

        for (listener in mListeners) {
            listener.onReceive(track!!)
        }

        if (track!!.distance > mDistanceForSpeech) {
            mTextToSpeech!!.speak(track!!.speechStr)
            mDistanceForSpeech = track!!.distance + track!!.speechDistance
        }

        updateNotification("Running", "${track!!.distance}m. | ${track!!.duration / 60}:${track!!.duration % 60}")
    }

    fun stopRecording(context: Context) {
        if (!isRecording || !paused) return
        isRecording = false
        paused = false
        mTextToSpeech?.destroy()
        mService?.unsubscribe(::onCurrentPointChanged)
        mService?.destroy(context)
        mService = null
    }

    fun registerListener(listener: ITrackRecordListener) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: ITrackRecordListener?) {
        mListeners.remove(listener)
    }
    //endregion

    private fun updateNotification(title: String, text: String) {
        val service = mService ?: return

        val notificationIntent = Intent(service, RecordTrackActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(service,0, notificationIntent, 0)
        mNotification = NotificationCompat.Builder(service, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        service.startForeground(NOTIFICATION_ID, mNotification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "RecordTrackService",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = mService!!.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}