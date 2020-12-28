package com.max.spirihin.mytracksdb.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.core.RecordState
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.yandex.mapkit.MapKitFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("0e9fede4-9954-4e61-b193-66191985d75d")
        MapKitFactory.initialize(this)
        Preferences.init(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = mutableListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permissions.all { p ->
                    ContextCompat.checkSelfPermission(
                            applicationContext, p) ==
                            PackageManager.PERMISSION_GRANTED
                }) {
            init()
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(permissions.toTypedArray(), 1)
            }
        }
    }

    private fun init() {
        TracksDatabase.init(this)

        findViewById<View>(R.id.btnStartEasyRun).setOnClickListener {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.EASY_RUN.toString())
            startActivity(intent)
        }

        findViewById<View>(R.id.btnStartWalking).setOnClickListener {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.WALKING.toString())
            startActivity(intent)
        }

        findViewById<View>(R.id.btnTracksList).setOnClickListener {
            startActivity(TracksListActivity::class.java)
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(SettingsActivity::class.java)
        }

        findViewById<View>(R.id.btnTest).setOnClickListener {
            startActivity(TestActivity::class.java)
        }

        if (TrackRecordManager.recordState != RecordState.NONE)
            startActivity(RecordTrackActivity::class.java)
    }

    private fun startActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != 1)
            return

        var missedPermissions = ""
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                missedPermissions += permissions[i] + " "
        }

        if (missedPermissions.isNullOrEmpty()) {
            init()
        } else {
            Toast.makeText(this, "You should grant all permissions. Restart app and do it. Missed permissions = $missedPermissions", Toast.LENGTH_LONG).show()
        }
    }
}