package com.max.spirihin.mytracksdb

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.TrackRecordManager.ITrackRecordListener
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import java.io.File
import java.util.*

class RecordTrackActivity : AppCompatActivity(), ITrackRecordListener {
    private var mTextView: TextView? = null
    private var mLog = ""
    private var mapView: MapView? = null
    private var mapObjects: MapObjectCollection? = null

    private var mMapZoomed = false

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_track)
        mapView = findViewById(R.id.mapview)
        if (mapView != null) mapObjects = mapView!!.map.mapObjects.addCollection()

        mTextView = findViewById<View>(R.id.textViewLog) as TextView
        TrackRecordManager.init(this)
        TrackRecordManager.registerListener(this)

        (findViewById<View>(R.id.btnStart) as Button).setOnClickListener {
            addLog("Started")
            try {
                TrackRecordManager.startRecording(this)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
        (findViewById<View>(R.id.btnStop) as Button).setOnClickListener {
            TrackRecordManager.stopRecording(this)
            val track = TrackRecordManager.track ?: return@setOnClickListener

            val json = track.toJSON()
            addLog("Stopped")
            addLog("Points count = " + track.points.size)
            addLog("DISTANCE = " + track.distance)
            fileInStorage("MyTrackksDB.txt").writeText(json)
        }
        findViewById<View>(R.id.btnLoad).setOnClickListener {
            addLog("Load track")
            val loaded: Track = Track.fromJSON(fileInStorage("MyTrackksDB.txt").readText()) ?: return@setOnClickListener
            addLog("Points count = " + loaded.points.size)
            addLog("DISTANCE = " + loaded.distance)
            showTrackOnMap(loaded, Color.BLUE, false)
        }
        findViewById<View>(R.id.btnLoadGPX).setOnClickListener {
            addLog("Load track GPX")
            val loaded: Track = Track.fromGPX(fileInStorage("MyTracksDB.gpx")) ?: return@setOnClickListener
            addLog("Points count = " + loaded.points.size)
            addLog("DISTANCE = " + loaded.distance)
            showTrackOnMap(loaded, Color.RED, false)
        }
        findViewById<View>(R.id.btnClearMap).setOnClickListener {
            addLog("clear map")
            mapObjects!!.clear()
        }
    }

    override fun onStop() {
        // Activity onStop call must be passed to both MapView and MapKit instance.
        if (mapView != null) {
            mapView!!.onStop()
            MapKitFactory.getInstance().onStop()
        }
        super.onStop()
    }

    override fun onStart() {
        // Activity onStart call must be passed to both MapView and MapKit instance.
        super.onStart()
        if (mapView == null) return
        MapKitFactory.getInstance().onStart()
        mapView!!.onStart()
    }

    private fun fileInStorage(fileName: String) : File {
        val folder = Environment.getExternalStorageDirectory().absolutePath
        return File(folder, fileName)
    }

    private fun showTrackOnMap(track: Track?, color: Int, clear: Boolean) {
        if (mapView == null || track == null) return

        val polylinePoints = ArrayList<Point>()
        for (point in track.points) {
            polylinePoints.add(Point(
                    point.latitude,
                    point.longitude)
            )
        }
        if (clear) mapObjects!!.clear()
        val polyline = mapObjects!!.addPolyline(Polyline(polylinePoints))
        polyline.strokeColor = color
        polyline.strokeWidth = 1f
        polyline.zIndex = 100.0f
        if (!mMapZoomed) {
            mapView!!.map.move(
                    CameraPosition(polylinePoints[0], 14.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.LINEAR, 0.0f),
                    null)
            mMapZoomed = true
        }
    }

    private fun addLog(log: String?) {
        mLog += "$log \n"
        mTextView!!.text = mLog
    }

    override fun onReceive(track: Track) {
        val newPoint = track.points[track.points.size - 1]
        addLog(newPoint.toString())
        showTrackOnMap(track, Color.BLACK, true)
    }
}