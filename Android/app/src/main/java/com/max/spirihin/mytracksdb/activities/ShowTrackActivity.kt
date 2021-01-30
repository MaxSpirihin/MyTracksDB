package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.core.getName
import com.max.spirihin.mytracksdb.ui.ShowTrackPageFragmentAdapter


class ShowTrackActivity : AppCompatActivity() {

    companion object {
        const val TRACK_ID_INTENT_STRING = "trackID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_track)

        val id = intent.getLongExtra(ShowTrackActivityOld.TRACK_ID_INTENT_STRING, 0)
        val track = TracksDatabase.loadTrackByID(id)

        if (track == null) {
            Toast.makeText(this, "There is no track with id $id in database. I am sorry(", Toast.LENGTH_LONG).show()
            return
        }

        val viewPager = findViewById<ViewPager>(R.id.view_pager);
        viewPager.adapter = ShowTrackPageFragmentAdapter(supportFragmentManager, this, track)

        // Передаём ViewPager в TabLayout
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager)

        findViewById<TextView>(R.id.tvExerciseType).text = track.exerciseType.getName()
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        (findViewById<ImageButton>(R.id.btnDelete)).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Delete track")
                    .setMessage("Do you really want to delete this track?")
                    .setIcon(R.drawable.icon_delete)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.deleteTrack(track)
                        Toast.makeText(this, "Track successfully deleted", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        findViewById<Button>(R.id.btnOldView).setOnClickListener {
            val intent = Intent(this, ShowTrackActivityOld::class.java)
            intent.putExtra(ShowTrackActivityOld.TRACK_ID_INTENT_STRING, track.id)
            startActivity(intent)
            finish()
        }
    }
}