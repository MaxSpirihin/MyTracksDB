package com.max.spirihin.mytracksdb.core

import android.util.JsonWriter
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.utilities.Print
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.HashMap

object TrackParser {

    //region parsing
    fun toJSON(track: Track): String {
        val stringWriter = StringWriter()
        val jsonWriter = JsonWriter(stringWriter)
        jsonWriter.beginObject() // begin root
        jsonWriter.name("parseVersion").value(2)
        jsonWriter.name("exerciseType").value(track.exerciseType.toString())
        jsonWriter.name("segments").beginArray()
        for (sector in track.segments) {
            jsonWriter.beginObject()
            jsonWriter.name("points").beginArray()
            for (point in sector.points) {
                jsonWriter.beginObject()
                jsonWriter.name("date").value(point.time.time)
                jsonWriter.name("latitude").value(point.latitude)
                jsonWriter.name("longitude").value(point.longitude)
                jsonWriter.name("accuracy").value(point.accuracy)
                jsonWriter.name("steps").value(point.steps)
                jsonWriter.name("heartrate").value(point.heartrate)
                jsonWriter.endObject()
            }
            jsonWriter.endArray()
            jsonWriter.endObject()//end segment
        }
        jsonWriter.endArray()

        if (track.weatherInfo != null)
            jsonWriter.name("weather").value(track.weatherInfo!!.json)

        jsonWriter.endObject() // end root
        return stringWriter.toString()
    }

    fun fromGPX(file: File?): Pair<Track?, HashMap<String, String>> {
        return try {
            val track = Track(ExerciseType.UNKNOWN)
            val params = HashMap<String, String>()

            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            doc.documentElement.normalize()
            val gpx = doc.getElementsByTagName("gpx").item(0) as Element
            val trk = gpx.getElementsByTagName("trk").item(0) as Element

            val gpxSegments = trk.getElementsByTagName("trkseg")
            for (temp in 0 until gpxSegments.length) {
                val segment = Segment()
                val gpxSegment = gpxSegments.item(temp) as Element
                val points = gpxSegment.getElementsByTagName("trkpt")
                for (i in 0 until points.length) {
                    val point = points.item(i) as Element
                    val latitude = point.getAttribute("lat").toDouble()
                    val longitude = point.getAttribute("lon").toDouble()
                    val timeStr = (point.getElementsByTagName("time").item(0) as Element).textContent
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val time = format.parse(timeStr)
                    segment.points.add(TrackPoint(time!!, latitude, longitude, 0.0, 0, 0))
                }
                track.segments.add(segment)
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

    fun fromJSON(id: Long, jsonString: String?): Track? {
        return try {
            val jsonRoot = JSONObject(jsonString ?: return null)
            if (!jsonRoot.has("parseVersion"))
                return fromJSONV1(id, jsonRoot)

            val exerciseType =
                    if (jsonRoot.has("exerciseType"))
                        ExerciseType.valueOf(jsonRoot.getString("exerciseType"))
                    else ExerciseType.UNKNOWN

            val track = Track(exerciseType)
            track.id = id
            val segmentsJson = jsonRoot.getJSONArray("segments")
            for (i in 0 until segmentsJson.length()) {
                val segment = Segment()
                track.segments.add(segment)
                val segmentJson = segmentsJson.getJSONObject(i)
                val pointsJson = segmentJson.getJSONArray("points")
                for (i in 0 until pointsJson.length()) {
                    val pointJson = pointsJson.getJSONObject(i)
                    val point = TrackPoint(
                            Date(pointJson.getLong("date")),
                            pointJson.getDouble("latitude"),
                            pointJson.getDouble("longitude"),
                            pointJson.getDouble("accuracy"),
                            if (pointJson.has("steps")) pointJson.getInt("steps") else 0,
                            if (pointJson.has("heartrate")) pointJson.getInt("heartrate") else 0
                    )

                    //TODO: move this condition somewhere else
                    //ignore points with gps mistakes
                    if (point.accuracy < Preferences.gpsMaxAccuracy)
                        segment.points.add(point)
                }
            }

            track.weatherInfo =  if (jsonRoot.has("weather")) WeatherInfo(jsonRoot.getString("weather")) else null
            track
        } catch (e: Exception) {
            Print.LogError(e.toString())
            null
        }
    }

    private fun fromJSONV1(id: Long, jsonRoot: JSONObject): Track? {
        return try {
            val jsonArray = jsonRoot.getJSONArray("Track")
            val track = Track(ExerciseType.EASY_RUN)//TODO: resave it
            val segment = Segment()
            track.segments.add(segment)
            track.id = id
            for (i in 0 until jsonArray.length()) {
                val pointJson = jsonArray.getJSONObject(i)
                segment.points.add(TrackPoint(
                        Date(pointJson.getLong("date")),
                        pointJson.getDouble("latitude"),
                        pointJson.getDouble("longitude"),
                        pointJson.getDouble("accuracy"),
                        if (pointJson.has("steps")) pointJson.getInt("steps") else 0,
                        if (pointJson.has("heartrate")) pointJson.getInt("heartrate") else 0
                ))
            }
            track
        } catch (e: Exception) {
            Print.LogError(e.toString())
            null
        }
    }
}