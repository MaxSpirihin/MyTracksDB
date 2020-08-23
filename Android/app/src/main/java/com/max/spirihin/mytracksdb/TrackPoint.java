package com.max.spirihin.mytracksdb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackPoint {

    private Date mTime;
    private double mLatitude;
    private double mLongitude;
    private double mAccuracy;

    public TrackPoint(Date mTime, double mLatitude, double mLongitude, double mAccuracy) {
        this.mTime = mTime;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mAccuracy = mAccuracy;
    }

    public Date getTime() {
        return mTime;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        return dateFormat.format(mTime) + " - " + mLatitude + " " + mLongitude + " " + mAccuracy;
    }
}
