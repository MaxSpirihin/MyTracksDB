package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.*
import com.max.spirihin.mytracksdb.core.*
import com.max.spirihin.mytracksdb.ui.YandexMap

class RecordTrackActivity : AppCompatActivity() {

    companion object {
        const val EXERCISE_TYPE_INTENT_STRING = "exerciseType"
    }

    private var textView: TextView? = null

    private var btnStart: Button? = null
    private var btnStop: Button? = null
    private var btnStopNoSave: Button? = null
    private var yandexMap : YandexMap? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_track)
        yandexMap = YandexMap(findViewById(R.id.mapview))

        textView = findViewById(R.id.textViewData)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnStopNoSave = findViewById(R.id.btnStopNoSave)

        btnStart!!.setOnClickListener {
            when (TrackRecordManager.recordState) {
                RecordState.RECORD -> {
                    TrackRecordManager.pauseRecording()
                }
                RecordState.PAUSE -> {
                    TrackRecordManager.resumeRecording()
                }
                else -> {
                    val exerciseType = ExerciseType.valueOf(
                            intent.getStringExtra(EXERCISE_TYPE_INTENT_STRING)
                                    ?: ExerciseType.UNKNOWN.toString()
                    )
                    TrackRecordManager.startRecording(this, exerciseType)
                }
            }
            updateState()
        }
        btnStop!!.setOnClickListener {
            if (TrackRecordManager.recordState != RecordState.PAUSE)
                return@setOnClickListener

            val track = TrackRecordManager.stopRecording(this, true)

            if (track != null) {
                val intent = Intent(this, ShowTrackActivity::class.java)
                intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, track.id)
                startActivity(intent)
            }

            finish()
        }

        btnStopNoSave!!.setOnClickListener {
            if (TrackRecordManager.recordState != RecordState.PAUSE)
                return@setOnClickListener

            AlertDialog.Builder(this)
                    .setTitle("Exit without save?")
                    .setMessage("Do you really to exit without saving track? Data will be lost")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TrackRecordManager.stopRecording(this, false)
                        finish()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }
    }

    override fun onStop() {
        yandexMap!!.onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        yandexMap!!.onStart()
    }

    override fun onResume() {
        super.onResume()

        if (TrackRecordManager.recordState == RecordState.NONE)
            TrackRecordManager.startListen(this)

        TrackRecordManager.subscribe(::onCurrentPointChanged)

        updateState()
        onCurrentPointChanged()
    }

    override fun onPause() {
        super.onPause()

        if (TrackRecordManager.recordState == RecordState.LISTEN)
            TrackRecordManager.stopListen(this)

        TrackRecordManager.unsubscribe(::onCurrentPointChanged)
    }

    private fun updateState() {

        val state = TrackRecordManager.recordState
        btnStart!!.text = when (state) {
            RecordState.RECORD -> "pause"
            RecordState.PAUSE -> "resume"
            else -> "start"
        }

        btnStop!!.isEnabled = state == RecordState.PAUSE
        btnStopNoSave!!.isEnabled = state == RecordState.PAUSE
    }

    private fun updateText(track: Track) {
        textView!!.text = track.infoStr
    }

    private fun onCurrentPointChanged() {
        runOnUiThread {
            val track = TrackRecordManager.track ?: return@runOnUiThread
            updateText(track)
            yandexMap!!.showTrack(track, Color.BLUE)
        }
    }
}