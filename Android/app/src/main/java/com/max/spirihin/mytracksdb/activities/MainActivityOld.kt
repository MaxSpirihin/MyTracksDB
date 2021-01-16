package com.max.spirihin.mytracksdb.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.core.RecordState
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.yandex.mapkit.MapKitFactory

class MainActivityOld : AppCompatActivity() {

    private var mLinearLayout : LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("0e9fede4-9954-4e61-b193-66191985d75d")
        MapKitFactory.initialize(this)
        Preferences.init(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_old)

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

        mLinearLayout = findViewById<LinearLayout>(R.id.linLayoutMain)

        addButton("EASY RUN") {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.EASY_RUN.toString())
            startActivity(intent)
        }

        addButton("WALKING") {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.WALKING.toString())
            startActivity(intent)
        }

        addButton("SKATES") {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.SKATES.toString())
            startActivity(intent)
        }

        addButton("SKIING") {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.SKIING.toString())
            startActivity(intent)
        }

        addButton("TEST RECORD") {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, ExerciseType.EASY_RUN.toString())
            intent.putExtra(RecordTrackActivity.USE_TEST_SERVICE_INTENT_STRING, true)
            startActivity(intent)
        }

        addButton("TRACKS LIST") {
            startActivity(TracksListActivity::class.java)
        }

        addButton("SETTINGS") {
            startActivity(SettingsActivity::class.java)
        }

        if (TrackRecordManager.recordState != RecordState.NONE)
            startActivity(RecordTrackActivity::class.java)
    }

    private fun addButton(text : String, action : () -> Unit) {
        val button = Button(this)
        button.text = text
        button.setOnClickListener{ action?.invoke()}
        mLinearLayout?.addView(button)
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