package com.max.spirihin.mytracksdb.utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object Preferences {

    private var sharedPreferences : SharedPreferences? = null

    fun init(activity: Activity){
        sharedPreferences = activity.getSharedPreferences("MY_TRACKS_DB_PREFS", Context.MODE_PRIVATE)
    }

    var gpsUpdateMeters: Int
    get() {
        return getInt("gps_update_meters", 1)
    }
    set(value) {
        setInt("gps_update_meters", value)
    }

    var gpsUpdateSeconds: Int
        get() {
            return getInt("gps_update_seconds", 1)
        }
        set (value) {
            setInt("gps_update_seconds", value)
        }

    var gpsMaxAccuracy: Int
        get() {
            return getInt("gps_max_accuracy", 20)
        }
        set(value) {
            setInt("gps_max_accuracy", value)
        }

    fun getInt(key: String, default: Int) : Int {
        return sharedPreferences!!.getInt(key, default)
    }

    fun setInt(key: String, value: Int) {
        with (sharedPreferences!!.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getBoolean(key: String, default: Boolean) : Boolean {
        return sharedPreferences!!.getBoolean(key, default)
    }

    fun setBoolean(key: String, value: Boolean) {
        with (sharedPreferences!!.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getString(key: String, default: String) : String {
        return sharedPreferences!!.getString(key, default) ?: default
    }

    fun setString(key: String, value: String) {
        with (sharedPreferences!!.edit()) {
            putString(key, value)
            apply()
        }
    }
}