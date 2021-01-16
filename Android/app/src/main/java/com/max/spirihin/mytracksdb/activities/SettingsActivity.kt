package com.max.spirihin.mytracksdb.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.room.Room
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.core.db.TracksRoomDatabase
import com.max.spirihin.mytracksdb.utilities.Print
import com.max.spirihin.mytracksdb.utilities.RoomBackup

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val etGPSSeconds = findViewById<EditText>(R.id.etGPSSeconds)
        val etGPSMeters = findViewById<EditText>(R.id.etGPSMeters)

        etGPSSeconds.setText(Preferences.gpsUpdateSeconds.toString())
        etGPSSeconds.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                Preferences.gpsUpdateSeconds = text.toString().toInt()
        }

        etGPSMeters.setText(Preferences.gpsUpdateMeters.toString())
        etGPSMeters.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                Preferences.gpsUpdateMeters = text.toString().toInt()
        }

        etGPSMeters.setText(Preferences.gpsMaxAccuracy.toString())
        etGPSMeters.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                Preferences.gpsMaxAccuracy = text.toString().toInt()
        }

        findViewById<View>(R.id.btnUpdateDB).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Update database")
                    .setMessage("Do you really want update database?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.updateDatabase()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        findViewById<View>(R.id.btnBackupDB).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Backup database")
                    .setMessage("Do you really want backup database?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.backupSqlFile()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        findViewById<View>(R.id.btnRestoreDB).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("STOP. WARNING!!!")
                    .setMessage("Do you really want restore database? You must be absolutely sure. Restore is making from \"for_restore\" " +
                            "file in MyTracksDB folder. Prepare it manually. Before restore extra backup will be made")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.restoreSqlFile()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }


    }
}