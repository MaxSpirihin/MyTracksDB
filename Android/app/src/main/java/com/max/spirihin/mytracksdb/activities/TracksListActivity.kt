package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.utilities.toShortString

class TracksListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)
    }

    override fun onResume() {
        super.onResume()

        val listView : ListView = findViewById(R.id.lvTracksList)

        val allTracks = TracksDatabase.loadAllTracks().sortedByDescending { track -> track.date }

        val tracksNames = arrayListOf<String>()
        for (track in allTracks) {
            tracksNames.add("${track.date.toShortString()} ${track.exerciseType} \n${track.distance}m. - ${track.duration}sec.")
        }

        val adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, tracksNames)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedTrack = allTracks[position]
            val intent = Intent(this, ShowTrackActivity::class.java)
            intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, selectedTrack.id)
            startActivity(intent)
        }
    }
}