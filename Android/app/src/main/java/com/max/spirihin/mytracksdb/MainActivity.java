package com.max.spirihin.mytracksdb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TrackRecordManager.ITrackRecordListener {

    TextView mTextView;

    private String mLog = "";
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private boolean mMapZoomed;
    EditText mEditTextSeconds;
    EditText mEditTextMeters;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("0e9fede4-9954-4e61-b193-66191985d75d");
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);
        if (mapView != null)
            mapObjects = mapView.getMap().getMapObjects().addCollection();

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Init();
        } else {
            // You can directly ask for the permission.
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    protected void onStop() {
        // Activity onStop call must be passed to both MapView and MapKit instance.

        if (mapView != null)
        {
            mapView.onStop();
            MapKitFactory.getInstance().onStop();
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        // Activity onStart call must be passed to both MapView and MapKit instance.
        super.onStart();

        if (mapView == null)
            return;

        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    void writeFile(String text) {
        try {
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myFile = new File(folder, "MyTrackksDB.txt");
            FileOutputStream fstream = new FileOutputStream(myFile);
            fstream.write(text.getBytes());
            fstream.close();
        } catch (FileNotFoundException e) {
            AddLog(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            AddLog(e.getMessage());
            e.printStackTrace();
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    String readFile(String fName) {
        try {
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myFile = new File(folder, fName);
            FileInputStream fstream = new FileInputStream(myFile);
            String result = convertStreamToString(fstream);
            fstream.close();
            return result;
        } catch (FileNotFoundException e) {
            AddLog(e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            AddLog(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void ShowTrackOnMap(Track track, int color, boolean clear)
    {
        if (mapView == null)
            return;

        ArrayList<Point> polylinePoints = new ArrayList<>();

        for (TrackPoint point : track.getPoints())
        {
            polylinePoints.add(new Point(
                    point.getLatitude(),
                    point.getLongitude())
            );
        }

        if (clear)
            mapObjects.clear();

        PolylineMapObject polyline = mapObjects.addPolyline(new Polyline(polylinePoints));
        polyline.setStrokeColor(color);
        polyline.setStrokeWidth(1);
        polyline.setZIndex(100.0f);

        if (!mMapZoomed) {
            mapView.getMap().move(
                    new CameraPosition(polylinePoints.get(0), 14.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.LINEAR, 0),
                    null);
            mMapZoomed = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != 1)
            return;

        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Init();
        }
    }

    @SuppressLint("MissingPermission")
    private void Init() {
        Log.d("MyLogs", "Init ");

        mEditTextSeconds = (EditText) findViewById(R.id.etSeconds);
        mEditTextMeters = (EditText) findViewById(R.id.etMeters);
        mTextView = (TextView) findViewById(R.id.textViewLog);
        TrackRecordManager.getInstance().Init(this);
        TrackRecordManager.getInstance().RegisterListener(this);

        ((Button) findViewById(R.id.btnStart)).setOnClickListener(v -> {
            int seconds = Integer.parseInt(mEditTextSeconds.getText().toString());
            int meters = Integer.parseInt(mEditTextMeters.getText().toString());
            AddLog("Started. Seconds = " + seconds + ". Meters = " + meters);
            try {
                TrackRecordManager.getInstance().StartRecording(this, seconds, meters);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        });

        ((Button) findViewById(R.id.btnStop)).setOnClickListener(v -> {
            TrackRecordManager.getInstance().StopRecording(this);
            Track track = TrackRecordManager.getInstance().getTrack();
            String json = track.ToJSON();
            AddLog("Stopped");
            AddLog("Points count = " + track.getPoints().size());
            AddLog("DISTANCE = " + track.getDistance());
            writeFile(json);
        });

        findViewById(R.id.btnLoad).setOnClickListener(v -> {
            AddLog("Load track");
            Track loaded = Track.FromJSON(readFile("MyTrackksDB.txt"));

            AddLog("Points count = " + loaded.getPoints().size());
            AddLog("DISTANCE = " + loaded.getDistance());

            ShowTrackOnMap(loaded, Color.BLUE, false);
        });

        findViewById(R.id.btnLoadGPX).setOnClickListener(v -> {
            AddLog("Load track GPX");

            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myFile = new File(folder, "MyTracksDB.gpx");

            Track loaded = Track.FromGPX(myFile);

            AddLog("Points count = " + loaded.getPoints().size());
            AddLog("DISTANCE = " + loaded.getDistance());

            ShowTrackOnMap(loaded, Color.RED, false);
        });

        findViewById(R.id.btnClearMap).setOnClickListener(v -> {
            AddLog("clear map");

            mapObjects.clear();
        });
    }

    private void AddLog(String log) {
        mLog += log + "\n";
        mTextView.setText(mLog);
    }

    @Override
    public void OnReceive(Track track) {
        TrackPoint newPoint = track.getPoints().get(track.getPoints().size() - 1);

        AddLog(newPoint.toString());

        ShowTrackOnMap(track, Color.BLACK, true);
    }
}