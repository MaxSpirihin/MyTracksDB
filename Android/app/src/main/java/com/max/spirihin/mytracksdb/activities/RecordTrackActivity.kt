package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.*
import com.max.spirihin.mytracksdb.core.TrackRecordManager.ITrackRecordListener
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.ui.YandexMap

class RecordTrackActivity : AppCompatActivity(), ITrackRecordListener {

    enum class RecordState {
        NONE,
        RECORD,
        PAUSE
    }

    private var textView: TextView? = null

    private var btnStart: Button? = null
    private var btnStop: Button? = null
    private var state: RecordState = RecordState.NONE
    private var yandexMap : YandexMap? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_track)
        yandexMap = YandexMap(findViewById(R.id.mapview))

        textView = findViewById(R.id.textViewData)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        updateState(if (TrackRecordManager.isRecording) RecordState.RECORD else RecordState.NONE)
        if (TrackRecordManager.isRecording && TrackRecordManager.track != null)
            onReceive(TrackRecordManager.track!!)

        btnStart!!.setOnClickListener {
            when (state) {
                RecordState.NONE -> {
                    updateState(RecordState.RECORD)
                    TrackRecordManager.startRecording(this)
                }
                RecordState.RECORD -> {
                    updateState(RecordState.PAUSE)
                    TrackRecordManager.pauseRecording(true)
                }
                RecordState.PAUSE -> {
                    updateState(RecordState.RECORD)
                    TrackRecordManager.pauseRecording(false)
                }
            }
        }
        btnStop!!.setOnClickListener {
            if (state != RecordState.PAUSE)
                return@setOnClickListener

            TrackRecordManager.stopRecording(this)
            val track = TrackRecordManager.track ?: return@setOnClickListener
            val id = TracksDatabase.saveTrack(track)

            val intent = Intent(this, ShowTrackActivity::class.java)
            intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, id)
            startActivity(intent)
            finish()
        }
    }

    override fun onStop() {
        yandexMap!!.onStop()
        TrackRecordManager.unregisterListener(this)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        TrackRecordManager.registerListener(this)
        yandexMap!!.onStart()
    }

    private fun updateState(newState : RecordState) {
        state = newState

        btnStart!!.text = when (state) {
            RecordState.NONE -> "start"
            RecordState.RECORD -> "pause"
            RecordState.PAUSE -> "resume"
        }

        btnStop!!.isEnabled = state == RecordState.PAUSE
    }

    private fun updateText(track: Track) {
        textView!!.text = track.infoStr
    }

    override fun onReceive(track: Track) {
        updateText(track)
        yandexMap!!.showTrack(track, Color.BLUE, true)
    }
}