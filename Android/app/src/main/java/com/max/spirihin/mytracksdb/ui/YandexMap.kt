package com.max.spirihin.mytracksdb.ui

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.core.math.MathUtils
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.core.TrackPoint
import com.max.spirihin.mytracksdb.utilities.Print
import com.max.spirihin.mytracksdb.utilities.Utils
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class YandexMap constructor(private val context: Activity, private val mapView: MapView){

    private val mapObjects: MapObjectCollection = mapView.map.mapObjects.addCollection()
    private var mPolyLines : MutableList<MapObject> = mutableListOf()
    private var mCurrentPos : MapObject? = null
    private var isMapZoomed = false

    /**
     * This method should be called from activity onStop
     */
    fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    /**
     * This method should be called from activity onStart
     */
    fun onStart() {
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    fun showCurrentPosition(trackPoint: TrackPoint?) {
        if (trackPoint == null)
            return

        if (mCurrentPos != null)
            mapObjects.remove(mCurrentPos!!)

        val point = Point(trackPoint.latitude, trackPoint.longitude)
        val placemark = mapObjects.addPlacemark(point)
        placemark.setIcon(ImageProvider.fromResource(context, R.raw.my_location))
        mCurrentPos = placemark

        zoomMap(point)
    }

    fun showTrack(track: Track?, color: Int) {
        if (track == null)
            return

        for (polyLines in mPolyLines)
            mapObjects.remove(polyLines)

        mPolyLines.clear()

        val allPoints = mutableListOf<Point>()

        for (sector in track.segments) {
            val polylinePoints = ArrayList<Point>()
            for (point in sector.points) {

                val point = Point(point.latitude, point.longitude)
                allPoints.add(point)
                polylinePoints.add(point)
            }

            val polyline = mapObjects.addPolyline(Polyline(polylinePoints))
            polyline.strokeColor = color
            polyline.strokeWidth = 1f
            polyline.zIndex = 100.0f
        }

        if (allPoints.isEmpty())
            return

        val latMin = allPoints.map { p -> p.latitude }.min()!!
        val latMax = allPoints.map { p -> p.latitude }.max()!!
        val longMin = allPoints.map { p -> p.longitude }.min()!!
        val longMax = allPoints.map { p -> p.longitude }.max()!!

        val center = Point((latMin + latMax) / 2, (longMin + longMax) / 2)

        zoomMap(center, getZoomTooShowAllPoints(center, allPoints))
    }

    private fun getZoomTooShowAllPoints(center: Point, allPoints : List<Point>) : Float {

        //compute max distance from center point in meters
        val maxRadius = allPoints.map { p ->
            val result = FloatArray(1)
            Location.distanceBetween(p.latitude, p.longitude, center.latitude, center.longitude, result)
            result[0]
        }.max()!!.toDouble()

        /*
            Dark magic here )))
            Ok, i will explain where the fuck from we have this crazy formula
            By reverse engeneering i found out that zoom depends on visible distance logarithmically
            After that optimal coefficients for that dependency was found (by test runs and proportion)
            If you want make zoom little more you can change additive coefficient (add about 0-2 not more)
            Do not change multiplicator
         */
        return (-1.43 * ln(maxRadius) + 23).toFloat()
    }

    private fun zoomMap(point: Point, zoom : Float = 14.0f) {
        if (!isMapZoomed) {
            isMapZoomed = true
            mapView.map.move(
                    CameraPosition(point, zoom, 0.0f, 0.0f),
                    Animation(Animation.Type.LINEAR, 0.0f),
                    null)
        }
    }

}