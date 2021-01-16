package com.max.spirihin.mytracksdb.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.*
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.utilities.Utils
import com.max.spirihin.mytracksdb.utilities.toShortString
import com.max.spirihin.mytracksdb.utilities.toStringFormat
import com.yandex.mapkit.MapKitFactory


class MainActivity : AppCompatActivity() {

    var layoutMain : LinearLayout? = null
    var tracksList : View? = null
    val startButtonsLayouts = mutableListOf<LinearLayout>()

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
        layoutMain = findViewById(R.id.linLayoutMain)

        createStartButtons(listOf(
                ExerciseType.EASY_RUN,
                ExerciseType.WALKING,
                ExerciseType.BICYCLE,
                ExerciseType.CONTROL_RUN,
                ExerciseType.SKATES,
                ExerciseType.SKIING
        ))

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createStartButtons(types: List<ExerciseType>) {
        val buttonsInRow = 2
        val margin = resources.getDimension(R.dimen.menu_main_margin).toInt()

        tracksList = inflateTracksListView()
        addToLinearLayout(layoutMain!!, tracksList!!, margin, margin, margin, 0)

        for (i in 0 until types.size / buttonsInRow) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            addToLinearLayout(layoutMain!!, linearLayout, margin / 2, margin, margin / 2, 0)
            for (j in 0 until buttonsInRow) {
                val index = i * buttonsInRow + j
                val exercise = if (index >= types.size) ExerciseType.UNKNOWN else types[i * buttonsInRow + j]
                addToLinearLayout(linearLayout, inflateStartButton(exercise), margin / 2, 0, margin / 2, 0)
            }
        }
    }

    private fun inflateTracksListView() : View {
        val view: View = LayoutInflater.from(this).inflate(
                R.layout.menu_main_show_tracks, null)
        view.findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this, TracksListActivity::class.java)
            startActivity(intent)
        }
        val lastTrack = TracksDatabase.loadAllTracks().sortedByDescending { t -> t.date }.firstOrNull()
        if (lastTrack != null) {
            view.findViewById<TextView>(R.id.tvInfo).text = "${lastTrack.exerciseType.getName()} ${lastTrack.date.toStringFormat("dd.MM")}"
            view.findViewById<TextView>(R.id.tvDistance).text = "${Utils.distanceToString(lastTrack.distance)}"
            view.findViewById<TextView>(R.id.tvDuration).text = "${Utils.secondsToString(lastTrack.duration)}"
            view.findViewById<ImageView>(R.id.ivExercityType).setImageResource(lastTrack.exerciseType.getIconId())
        }
        return view
    }

    private fun inflateStartButton(exerciseType: ExerciseType) : View {
        val view: View = LayoutInflater.from(this).inflate(
                R.layout.menu_main_start, null)
        view.findViewById<ImageView>(R.id.image).setImageResource(exerciseType.getMenuMainIconId())
        view.findViewById<TextView>(R.id.text).text = exerciseType.getName().toUpperCase()
        view.findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this, RecordTrackActivity::class.java)
            intent.putExtra(RecordTrackActivity.EXERCISE_TYPE_INTENT_STRING, exerciseType.toString())
            startActivity(intent)
        }
        return view
    }

    private fun addToLinearLayout(
            linearLayout: LinearLayout,
            view: View,
            marginLeft: Int,
            marginTop: Int,
            marginRight: Int,
            marginBottom: Int) {
        val lp = LinearLayout.LayoutParams(
                if (linearLayout.orientation == LinearLayout.HORIZONTAL) 0 else LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        )
        lp.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        view.layoutParams = lp
        linearLayout.addView(view)
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