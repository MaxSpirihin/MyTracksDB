package com.max.spirihin.mytracksdb.core

import android.location.Location
import android.util.JsonWriter
import com.max.spirihin.mytracksdb.utilities.Print
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.HashMap
import com.max.spirihin.mytracksdb.utilities.toShortString

class Track {

    //region attrubutes
    val points: ArrayList<TrackPoint> = ArrayList()
    var id: Int = 0
    //endregion

    //region properties
    val distance: Int
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
            return sum.toInt()
        }

    val startTime: Date
        get() = if (points.size < 1) Date() else points[0].time

    /* full duration in seconds */
    val duration: Int
        get() = if (points.size < 2) 0 else ((points.last().time.time - points.first().time.time) / 1000).toInt()

    /* pace in seconds */
    val pace: Int
        get() = if (distance > 0) duration * 1000 / distance else 0

    val timeStr: String
        get() = startTime.toShortString()

    val speechStr: String
        get() {
            return "Pass $distance meters. Pace is ${pace / 60} minutes ${pace % 60} seconds"
        }

    val infoStr: String
        get() {
            return "time=${secondsToString(duration)}\ndistance=${distance}\npace=${secondsToString(pace)}\npoints=${points.size}\nsteps=${points.last().steps}"
        }
    //endregion

    //region public methods
    fun addPoint(location: Location, steps: Int): TrackPoint {
        val point = TrackPoint(
                Calendar.getInstance().time,
                location.latitude,
                location.longitude,
                location.accuracy.toDouble(),
                steps
        )

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
            jsonWriter.name("steps").value(point.steps)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.endObject() // end root
        return stringWriter.toString()
    }

    companion object {
        fun fromGPX(file: File?): Pair<Track?, HashMap<String, String>> {
            return try {
                val track = Track()
                val params = HashMap<String, String>()

                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                val doc = dBuilder.parse(file)
                doc.documentElement.normalize()
                val gpx = doc.getElementsByTagName("gpx").item(0) as Element
                val trk = gpx.getElementsByTagName("trk").item(0) as Element

                val segments = trk.getElementsByTagName("trkseg")
                for (temp in 0 until segments.length) {
                    val segment = segments.item(temp) as Element
                    val points = segment.getElementsByTagName("trkpt")
                    for (i in 0 until points.length) {
                        val point = points.item(i) as Element
                        val latitude = point.getAttribute("lat").toDouble()
                        val longitude = point.getAttribute("lon").toDouble()
                        val timeStr = (point.getElementsByTagName("time").item(0) as Element).textContent
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        val time = format.parse(timeStr)
                        track.points.add(TrackPoint(time!!, latitude, longitude, 0.0, 0))
                    }
                }
                val exerciseInfos = (gpx.getElementsByTagName("exerciseinfo").item(0) as Element).childNodes
                for (temp in 0 until exerciseInfos.length) {
                    val param = exerciseInfos.item(temp) as? Element
                    if (param != null)
                        params[param.tagName] = param.textContent
                }

                Pair(track, params)
            } catch (e: Exception) {
                Print.LogError(e.toString())
                Pair(null, HashMap())
            }
        }

        fun fromJSON(id: Int, jsonString: String?): Track? {
            return try {
                val jsonRoot = JSONObject(jsonString ?: return null)
                val jsonArray = jsonRoot.getJSONArray("Track")
                val track = Track()
                track.id = id
                for (i in 0 until jsonArray.length()) {
                    val pointJson = jsonArray.getJSONObject(i)
                    track.points.add(TrackPoint(
                            Date(pointJson.getLong("date")),
                            pointJson.getDouble("latitude"),
                            pointJson.getDouble("longitude"),
                            pointJson.getDouble("accuracy"),
                            if (pointJson.has("steps")) pointJson.getInt("steps") else 0
                    ))
                }
                track
            } catch (e: Exception) {
                Print.LogError(e.toString())
                null
            }
        }

        private fun secondsToString(seconds : Int) : String {
            val h = seconds / 3600
            val m = (seconds / 60) % 60
            val s = seconds % 60
            return "${if (h > 0) "${String.format("%02d",h)}:" else ""}${String.format("%02d",m)}:${String.format("%02d",s)}"
        }
    }
    //endregion
}