package com.max.spirihin.mytracksdb

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.TrackRecordManager.ITrackRecordListener

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
        TrackRecordManager.init(this)
        TrackRecordManager.registerListener(this)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        updateState(if (TrackRecordManager.isRecording) RecordState.RECORD else RecordState.NONE)

        btnStart!!.setOnClickListener {
            when (state) {
                RecordState.NONE -> {
                    updateState(RecordState.RECORD)
                    TrackRecordManager.startRecording(this)
                }
                RecordState.RECORD -> {
                    updateState(RecordState.PAUSE)
                    //TODO TrackRecordManager.pauseRecording(this)
                }
                RecordState.PAUSE -> {
                    updateState(RecordState.RECORD)
                    //TODO TrackRecordManager.resumeRecording(this)
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
        textView!!.text = "time=${track.duration}\ndistance=${track.distance}\npoints=${track.points.size}"
    }

    override fun onReceive(track: Track) {
        updateText(track)
        yandexMap!!.showTrack(track, Color.BLUE, true)
    }
}