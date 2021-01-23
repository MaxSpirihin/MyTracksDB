package com.max.spirihin.mytracksdb.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.utilities.Utils
import org.w3c.dom.Text


class ShowTrackFragmentTrack(val track : Track) : Fragment() {

    private var yandexMap: YandexMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.show_track_track_fragment, container, false)

        yandexMap = YandexMap(activity!!, view.findViewById(R.id.mapview))
        yandexMap!!.showTrack(track, Color.BLUE)

        view.findViewById<TextView>(R.id.tvDistance).text = Utils.distanceToStringShort(track.distance, 2)
        val titles = mutableListOf<String>()
        val values = mutableListOf<String>()

        titles.add("Duration")
        values.add(Utils.secondsToString(track.duration))

        titles.add("Pace")
        values.add(Utils.paceToString(track.pace, false))

        if (track.averageHeartrate > 0) {
            titles.add("Heartrate")
            values.add(track.averageHeartrate.toString())
        } else {
            titles.add("Cadence")
            values.add(track.cadence.toString())
        }

        view.findViewById<TextView>(R.id.tvTitle1).text = titles[0]
        view.findViewById<TextView>(R.id.tvValue1).text = values[0]
        view.findViewById<TextView>(R.id.tvTitle2).text = titles[1]
        view.findViewById<TextView>(R.id.tvValue2).text = values[1]
        view.findViewById<TextView>(R.id.tvTitle3).text = titles[2]
        view.findViewById<TextView>(R.id.tvValue3).text = values[2]

        return view
    }

    override fun onStop() {
        yandexMap!!.onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        yandexMap!!.onStart()
    }
}