package com.max.spirihin.mytracksdb.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import org.json.JSONException
import org.json.JSONObject


class TestActivity : AppCompatActivity() {

    var textView : TextView? = null
    var mRequestQueue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        textView = findViewById<TextView>(R.id.textView)

        mRequestQueue = Volley.newRequestQueue(this)

        val track = TracksDatabase.loadAllTracks().first().getTrack()
        getWeather("https://api.openweathermap.org/data/2.5/weather?lat=${track!!.segments[0].points[0].latitude}&lon=${track!!.segments[0].points[0].longitude}&appid=3802dce820b4e594a22733a939aba1ec")
    }

    private fun getWeather(url: String) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    textView!!.text = String.format("dgffdg%s","aa") + "Response: %s".format(response.toString())
                },
                Response.ErrorListener { error ->
                    // TODO: Handle error
                }
        )
        mRequestQueue?.add(jsonObjectRequest) // добавляем запрос в очередь
    }

}