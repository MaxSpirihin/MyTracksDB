package com.max.spirihin.mytracksdb

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class ShowTrackActivity : AppCompatActivity() {

    companion object {
        const val TRACK_ID_INTENT_STRING = "trackID"
    }

    private var textView: TextView? = null
    private var yandexMap : YandexMap? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_track)
        yandexMap = YandexMap(findViewById(R.id.mapview))
        textView = findViewById(R.id.textViewData)

        val id = intent.getIntExtra(TRACK_ID_INTENT_STRING, 0)
        val track = TracksDatabase.loadTrackByID(id)

        if (track == null) {
            Toast.makeText(this, "track is null",Toast.LENGTH_LONG).show()
            return
        }

        textView!!.text = "time=${track.duration}\ndistance=${track.distance}\npoints=${track.points.size}"
        yandexMap!!.showTrack(track, Color.BLUE, false)

        (findViewById<Button>(R.id.btnLoadGPX)).setOnClickListener {

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
}