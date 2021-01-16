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
        return sharedPreferences!!.getInt("gps_update_meters", 1)
    }
    set(value) {
        with (sharedPreferences!!.edit()) {
            putInt("gps_update_meters", value)
            apply()
        }
    }

    var gpsUpdateSeconds: Int
        get() {
            return sharedPreferences!!.getInt("gps_update_seconds", 1)
        }
        set(value) {
            with (sharedPreferences!!.edit()) {
                putInt("gps_update_seconds", value)
                apply()
            }
        }

    var gpsMaxAccuracy: Int
        get() {
            return sharedPreferences!!.getInt("gps_max_accuracy", 20)
        }
        set(value) {
            with (sharedPreferences!!.edit()) {
                putInt("gps_max_accuracy", value)
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