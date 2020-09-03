package com.max.spirihin.mytracksdb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class TracksListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)
    }

    override fun onResume() {
        super.onResume()

        val listView : ListView = findViewById(R.id.lvTracksList);

        val allTracks = TracksDatabase.loadAllTracks()
        allTracks.sortBy { track -> track.startTime }

        var tracksNames = arrayListOf<String>()
        for (track: Track in allTracks) {
            tracksNames.add("${track.timeStr}  (${track.id})\n${track.distance}m. -- ${track.duration}sec.")
        }

        val adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, tracksNames);
        listView.adapter = adapter;
        listView.setOnItemClickListener {parent,view, position, id ->
            val selectedTrack = allTracks[position]
            val intent = Intent(this, ShowTrackActivity::class.java)
            intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, selectedTrack.id)
            startActivity(intent)
        }
    }
}