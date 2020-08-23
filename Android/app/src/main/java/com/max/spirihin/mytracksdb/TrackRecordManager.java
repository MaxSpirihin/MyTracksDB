package com.max.spirihin.mytracksdb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import java.util.HashSet;

public class TrackRecordManager {

    //region testregion
    public interface ITrackRecordListener
    {
        void OnReceive(Track track);
    }

    private static TrackRecordManager mInstance;

    public static TrackRecordManager getInstance()
    {
        if (mInstance == null)
            mInstance = new TrackRecordManager();

        return mInstance;
    }

    //endregion

    HashSet<ITrackRecordListener> mListeners = new HashSet<ITrackRecordListener>();

    private Track mTrack;
    private boolean mRecording;
    private boolean mInited;

    //TODO: move it from here
    public int metersForUpdate = 0;
    public int secondsForUpdate = 0;

    private TrackRecordManager(){}

    public void Init(Activity activity) {
        if (mInited) {
            Toast.makeText(activity.getApplicationContext(), "TrackRecordManager already inited", Toast.LENGTH_LONG).show();
            return;
        }
        mInited = true;
    }

    @SuppressLint("MissingPermission")
    public void StartRecording(Activity activity, int seconds, int meters) throws Exception {

        if (mRecording)
            throw new Exception("Record is already running. Please call StopRecording");

        mRecording = true;

        Log.d("MyLogs", "Start record track");
        mTrack = new Track();

        //TODO: check if service is already running
        metersForUpdate = meters;
        secondsForUpdate = seconds;

        activity.startService(new Intent(activity, LocationService.class));
    }

    public void AddTrackPoint(Location location)
    {
        if (!mInited || !mRecording)
            return;

        if (location == null)
            return;

        mTrack.addPoint(location);

        for (ITrackRecordListener listener:mListeners) {
            listener.OnReceive(getTrack());
        }
    }

    public void StopRecording(Activity activity)
    {
        if (!mRecording)
            return;

        mRecording = false;
        activity.stopService(new Intent(activity, LocationService.class));
    }

    public Track getTrack()
    {
        return mTrack;
    }

    public void RegisterListener(ITrackRecordListener listener)
    {
        mListeners.add(listener);
    }

    public void UnregisterListener(ITrackRecordListener listener)
    {
        mListeners.remove(listener);
    }
}
