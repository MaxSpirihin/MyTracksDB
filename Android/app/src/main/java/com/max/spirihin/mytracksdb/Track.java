package com.max.spirihin.mytracksdb;

import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Track {

    private ArrayList<TrackPoint> mPoints;

    public Track() {
        mPoints = new ArrayList<TrackPoint>();
    }

    public TrackPoint addPoint(Location location) {
        TrackPoint point = new TrackPoint(Calendar.getInstance().getTime(), location.getLatitude(), location.getLongitude(), location.getAccuracy());
        mPoints.add(point);
        return point;
    }

    public double getDistance()
    {
        double sum = 0.0;
        for (int i = 0; i < mPoints.size() - 1; i++) {
            TrackPoint p1 = mPoints.get(i);
            TrackPoint p2 = mPoints.get(i + 1);
            float[] result = new float[1];
            Location.distanceBetween(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude(), result);
            sum += result[0];
        }
        return sum;
    }

    public ArrayList<TrackPoint> getPoints() {
        return mPoints;
    }

    public String ToJSON() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        try {
            jsonWriter.beginObject();// begin root

            jsonWriter.name("Track").beginArray();
            for (TrackPoint point : mPoints) {
                jsonWriter.beginObject();
                jsonWriter.name("date").value(point.getTime().getTime());
                jsonWriter.name("latitude").value(point.getLatitude());
                jsonWriter.name("longitude").value(point.getLongitude());
                jsonWriter.name("accuracy").value(point.getLongitude());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();// end websites

            // end root
            jsonWriter.endObject();

            return stringWriter.toString();

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static Track FromJSON(String jsonString) {
        JSONObject jsonRoot = null;
        try {
            jsonRoot = new JSONObject(jsonString);

            JSONArray jsonArray = jsonRoot.getJSONArray("Track");
            Track track = new Track();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pointJson = jsonArray.getJSONObject(i);
                track.mPoints.add(new TrackPoint(
                        new Date(pointJson.getLong("date")),
                        pointJson.getDouble("latitude"),
                        pointJson.getDouble("longitude"),
                        pointJson.getDouble("accuracy"))
                );
            }
            return track;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Track FromGPX(File file) {


        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            Element gpx = (Element)doc.getElementsByTagName("gpx").item(0);
            NodeList trkList = gpx.getElementsByTagName("trk");
            Element trk = (Element)trkList.item(0);

            Track track = new Track();

            NodeList segments = trk.getElementsByTagName("trkseg");

            for (int temp = 0; temp < segments.getLength(); temp++) {
                Element segment = (Element) segments.item(temp);
                NodeList points = segment.getElementsByTagName("trkpt");
                for (int i = 0; i < points.getLength(); i++) {

                    Element point = (Element) points.item(i);

                    double latitude = Double.parseDouble(point.getAttribute("lat"));
                    double longitude = Double.parseDouble(point.getAttribute("lon"));

                    track.mPoints.add(new TrackPoint(Calendar.getInstance().getTime(), latitude, longitude, 0));
                }
            }

            return track;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e)  {
        }catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

}
