package com.max.spirihin.mytracksdb.ui

import com.max.spirihin.mytracksdb.core.Track
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import java.util.ArrayList

class YandexMap constructor(mapView: MapView){

    private val mapView: MapView = mapView
    private val mapObjects: MapObjectCollection? = mapView.map.mapObjects.addCollection()

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

    fun showTrack(track: Track?, color: Int, clear: Boolean) {
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
        zoomMap(polylinePoints[0])
    }

    private fun zoomMap(point: Point) {
        if (!isMapZoomed) {
            mapView.map.move(
                    CameraPosition(point, 14.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.LINEAR, 0.0f),
                    null)
            isMapZoomed = true
        }
    }
}