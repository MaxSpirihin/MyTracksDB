package com.max.spirihin.mytracksdb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val etGPSSeconds = findViewById<EditText>(R.id.etGPSSeconds);
        val etGPSMeters = findViewById<EditText>(R.id.etGPSMeters);

        etGPSSeconds.setText(Preferences.gpsUpdateSeconds.toString());
        etGPSSeconds.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                Preferences.gpsUpdateSeconds = text.toString().toInt()
        }

        etGPSMeters.setText(Preferences.gpsUpdateMeters.toString());
        etGPSMeters.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                Preferences.gpsUpdateMeters = text.toString().toInt()
        }
    }
}