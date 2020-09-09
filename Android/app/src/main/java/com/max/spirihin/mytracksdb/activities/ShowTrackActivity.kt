package com.max.spirihin.mytracksdb.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.ui.YandexMap


class ShowTrackActivity : AppCompatActivity() {

    companion object {
        const val TRACK_ID_INTENT_STRING = "trackID"
    }

    private var textView: TextView? = null
    private var yandexMap: YandexMap? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_track)
        yandexMap = YandexMap(findViewById(R.id.mapview))
        textView = findViewById(R.id.textViewData)

        val id = intent.getIntExtra(TRACK_ID_INTENT_STRING, 0)
        val track = TracksDatabase.loadTrackByID(id)

        if (track == null) {
            Toast.makeText(this, "track is null", Toast.LENGTH_LONG).show()
            return
        }

        textView!!.text = track.infoStr
        yandexMap!!.showTrack(track, Color.BLUE, true)

        var gpxLoaded = false
        (findViewById<Button>(R.id.btnLoadGPX)).setOnClickListener {
            if (gpxLoaded)
                return@setOnClickListener

            gpxLoaded = true

            val gpxData = TracksDatabase.tryLoadGPXForTrack(track)
            val trackFromGPX = gpxData.first
            val gpxParams = gpxData.second

            if (trackFromGPX == null) {
                Toast.makeText(this, "gpx not found", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            yandexMap!!.showTrack(trackFromGPX, Color.RED, false)
            var text = textView!!.text.toString() + "\n\nFrom GPX\n${trackFromGPX.infoStr}\n"
            for (kvp in gpxParams)
                text += "${kvp.key}=${kvp.value}\n"

            textView!!.text = text
        }

        (findViewById<Button>(R.id.btnDelete)).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Delete track")
                    .setMessage("Do you really want to delete this track?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.deleteTrack(track)
                        Toast.makeText(this, "Track successfully deleted", Toast.LENGTH_LONG).show()
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
}