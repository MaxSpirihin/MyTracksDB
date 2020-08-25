package com.max.spirihin.mytracksdb

import android.location.Location
import android.util.JsonWriter
import android.util.Log
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class Track {

    //region properties
    val points: ArrayList<TrackPoint> = ArrayList()
    var id : Int = 0

    val distance: Double
        get() {
            //TODO we shouldn't compute this all the time, we need cache value and increment in addPoint
            var sum = 0.0
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val result = FloatArray(1)
                Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, result)
                sum += result[0]
            }
            return sum
        }

    /* full duration in seconds */
    val duration: Long
        get() = if (points.size < 2) 0 else (points.last().time.time - points.first().time.time) / 1000

    val timeStr: String
        get() {
            if (points.isEmpty())
                return ""
            val dateFormat: DateFormat = SimpleDateFormat("dd.MM - hh:mm:ss")
            return dateFormat.format(points[0].time)
        }
    //endregion

    //region public methods
    fun addPoint(location: Location): TrackPoint {
        val point = TrackPoint(Calendar.getInstance().time, location.latitude, location.longitude, location.accuracy.toDouble())
        points.add(point)
        return point
    }
    //endregion

    //region parsing
    fun toJSON(): String {
        val stringWriter = StringWriter()
        val jsonWriter = JsonWriter(stringWriter)
        jsonWriter.beginObject() // begin root
        jsonWriter.name("Track").beginArray()
        for (point in points) {
            jsonWriter.beginObject()
            jsonWriter.name("date").value(point.time.time)
            jsonWriter.name("latitude").value(point.latitude)
            jsonWriter.name("longitude").value(point.longitude)
            jsonWriter.name("accuracy").value(point.accuracy)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.endObject() // end root
        return stringWriter.toString()
    }

    companion object {
        fun fromGPX(file: File?): Track? {
            return try {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                val doc = dBuilder.parse(file)
                doc.documentElement.normalize()
                val gpx = doc.getElementsByTagName("gpx").item(0) as Element
                val trkList = gpx.getElementsByTagName("trk")
                val trk = trkList.item(0) as Element
                val track = Track()
                val segments = trk.getElementsByTagName("trkseg")
                for (temp in 0 until segments.length) {
                    val segment = segments.item(temp) as Element
                    val points = segment.getElementsByTagName("trkpt")
                    for (i in 0 until points.length) {
                        val point = points.item(i) as Element
                        val latitude = point.getAttribute("lat").toDouble()
                        val longitude = point.getAttribute("lon").toDouble()
                        track.points.add(TrackPoint(Calendar.getInstance().time, latitude, longitude, 0.0))
                    }
                }
                track
            } catch (e: Exception) {
                Log.e("myLogs", e.message ?: "")
                null
            }
        }

        fun fromJSON(jsonString: String?): Track? {
            return try {
                val jsonRoot = JSONObject(jsonString ?: return null)
                val jsonArray = jsonRoot.getJSONArray("Track")
                val track = Track()
                for (i in 0 until jsonArray.length()) {
                    val pointJson = jsonArray.getJSONObject(i)
                    track.points.add(TrackPoint(
                            Date(pointJson.getLong("date")),
                            pointJson.getDouble("latitude"),
                            pointJson.getDouble("longitude"),
                            pointJson.getDouble("accuracy"))
                    )
                }
                track
            } catch (e: Exception) {
                Log.e("myLogs", e.message ?: "")
                null
            }
        }
    }
    //endregion
}