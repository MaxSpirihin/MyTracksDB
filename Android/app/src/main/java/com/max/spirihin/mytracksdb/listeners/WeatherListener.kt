package com.max.spirihin.mytracksdb.listeners

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.max.spirihin.mytracksdb.core.WeatherInfo
import com.max.spirihin.mytracksdb.utilities.Print

class WeatherListener {
    private var mRequestQueue :RequestQueue? = null
    private var mRequestInProcess = false
    private var mTryCount = 0
    var weatherInfo : WeatherInfo? = null
        private set

    fun startListen(context: Context) {
        mRequestQueue = Volley.newRequestQueue(context)
    }

    fun onLocationChanged(latitude : Double, longitude : Double) {
        if (weatherInfo != null || mRequestInProcess || mTryCount >= 3)
            return

        mRequestInProcess = true
        mTryCount++

        val url = getURL(latitude, longitude)
        Print.Log("[WeatherListener] Request to $url")
        val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener { response ->
                    Print.Log("[WeatherListener] Load weather $response")
                    weatherInfo = WeatherInfo(response.toString())
                    mRequestInProcess = false
                },
                Response.ErrorListener { error ->
                    Print.LogError("[WeatherListener] Can't load weather $error")
                    mRequestInProcess = false
                }
        )
        mRequestQueue!!.add(request)
    }

     fun stopListen() {
        mRequestQueue?.cancelAll { _ -> true }
    }

    private fun getURL(latitude: Double, longitude: Double): String {
        return String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=3802dce820b4e594a22733a939aba1ec", latitude, longitude)
    }
}