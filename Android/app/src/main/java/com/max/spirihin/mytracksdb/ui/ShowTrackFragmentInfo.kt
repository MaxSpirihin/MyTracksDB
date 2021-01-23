package com.max.spirihin.mytracksdb.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.Track


class ShowTrackFragmentInfo(val track : Track) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.show_track_info_fragment, container, false)
        val textView = view as TextView
        textView.text = track.infoStr
        return view
    }
}