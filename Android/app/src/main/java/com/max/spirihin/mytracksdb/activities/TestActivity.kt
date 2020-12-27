package com.max.spirihin.mytracksdb.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.db.Track
import com.max.spirihin.mytracksdb.core.db.TracksRoomDatabase

class TestActivity : AppCompatActivity() {

    private var textView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter_test)

        textView = findViewById<TextView>(R.id.tvTest)
    }

}