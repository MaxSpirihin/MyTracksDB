package com.max.spirihin.mytracksdb.core

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
    //region constants
    const val NOTIFICATION_ID = 10
    const val CHANNEL_ID = "RecordTrackService"
    //endregion

    //region attributes
    private var mDistanceForSpeech : Int = 0
    private var mObservers = HashSet<() -> Unit>()
    private var mService : TrackPointsProviderService? = null
    private var mTextToSpeech: TextToSpeechHelper? = null
    private var mNotification: Notification? = null
    //endregion

    //region properties
    var track: Track? = null
        private set
    var recordState = RecordState.NONE
        private set
    //endregion

    //region public methods
    fun attachPointsProvider(service : TrackPointsProviderService){
        if (mService != null)
            throw java.lang.Exception("TrackRecordManager already has points provider")
        mService = service
        mService?.subscribe(::onCurrentPointChanged)
        createNotificationChannel()
        updateNotification("Running", "")
    }

    fun startListen(context: Context) {
        if (recordState != RecordState.NONE)
            throw Exception("Cant start listen from state $recordState")

        Print.Log("Start listen location")
        recordState = RecordState.LISTEN

        //start points provider service
        // context.startService(Intent(context, RecordTrackService::class.java))
        context.startService(Intent(context, ReplayTrackService::class.java))
    }

    fun stopListen(context: Context) {
        if (recordState != RecordState.LISTEN)
            throw Exception("Cant stop listen from state $recordState")

        Print.Log("Stop listen location")
        recordState = RecordState.NONE
        destroyService(context)
    }

    fun startRecording(context: Context, exerciseType: ExerciseType) {
        if (recordState != RecordState.LISTEN)
            throw Exception("Cant start recording from state $recordState")

        Print.Log("Start record track")
        recordState = RecordState.RECORD
        track = Track(exerciseType)
        mTextToSpeech = TextToSpeechHelper(context.applicationContext)
        mDistanceForSpeech = track!!.speechDistance
    }

    fun getLastPoint() : TrackPoint? {
        return mService?.getCurrentPoint()
    }

    fun pauseRecording() {
        if (recordState != RecordState.RECORD)
            throw Exception("Cant pause recording from state $recordState")

        Print.Log("Pause record")
        recordState = RecordState.PAUSE

        val currentPoint = mService!!.getCurrentPoint()
        if (currentPoint != null)
            track?.addPoint(currentPoint)

        track?.newSegment()
    }

    fun resumeRecording() {
        if (recordState != RecordState.PAUSE)
            throw Exception("Cant resume recording from state $recordState")

        Print.Log("Resume record")
        recordState = RecordState.RECORD

        val currentPoint = mService!!.getCurrentPoint()
        if (currentPoint != null)
            track?.addPoint(currentPoint)
    }

    fun stopRecording(context: Context, saveTrack: Boolean) : Track? {
        if (recordState != RecordState.PAUSE)
            throw Exception("Cant stop recording from state $recordState")

        val track = TrackRecordManager.track ?: return null

        if (saveTrack)
            track.id = TracksDatabase.saveTrack(track)

        Print.Log("Stop record")
        recordState = RecordState.NONE

        mTextToSpeech?.destroy()
        destroyService(context)
        TrackRecordManager.track = null

        return track
    }

    fun subscribe(observer : () -> Unit) {
        mObservers.add(observer)
    }

    fun unsubscribe(observer : () -> Unit) {
        mObservers.remove(observer)
    }
    //endregion

    private fun destroyService(context: Context){
        mService?.unsubscribe(::onCurrentPointChanged)
        mService?.destroy(context)
        mService = null
    }

    private fun onCurrentPointChanged(point : TrackPoint) {
        for (observer in mObservers) {
            observer?.invoke()
        }

        if (recordState != RecordState.RECORD || track == null)
            return

        track!!.addPoint(point)

        if (track!!.distance > mDistanceForSpeech) {
            mTextToSpeech!!.speak(track!!.speechStr)
            mDistanceForSpeech = track!!.distance + track!!.speechDistance
        }

        updateNotification("Running", "${track!!.distance}m. | ${track!!.duration / 60}:${track!!.duration % 60}")
    }

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