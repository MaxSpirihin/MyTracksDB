package com.max.spirihin.mytracksdb

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.yandex.mapkit.MapKitFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("0e9fede4-9954-4e61-b193-66191985d75d")
        MapKitFactory.initialize(this)
        Preferences.init(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            init()
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                        1)
            }
        }
    }

    private fun init() {
        findViewById<View>(R.id.btnStartRecording).setOnClickListener {
            val intent = Intent(this, RecordTrackActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnTracksList).setOnClickListener {
            //todo
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != 1) {
            Toast.makeText(this, "You should grant all permissions. Restart app and do it", Toast.LENGTH_LONG).show();
            return
        }

        val granted = grantResults.isNotEmpty() && grantResults.all { res -> res == PackageManager.PERMISSION_GRANTED }

        if (granted) {
            init()
        } else {
            Toast.makeText(this, "You should grant all permissions. Restart app and do it", Toast.LENGTH_LONG).show()
        }
    }
}