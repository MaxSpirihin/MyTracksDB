package com.max.spirihin.mytracksdb.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.utilities.UIUtils
import com.max.spirihin.mytracksdb.utilities.Utils


class ShowTrackFragmentSegments(val track : Track) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.show_track_segments_fragment, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.linearLayout)

        configurePaceSegments(layout)

        if (track.averageHeartrate > 0)
            configureHeartrateSegments(layout)

        return view
    }

    private fun configurePaceSegments(layout: LinearLayout) {
        val segments = track.getPaceAtSegments().toList()
        if (segments.size < 2)
            return

        val maxValue = segments.map { s -> s.second }.max()!! + 60.0
        val fastestPaceIndex = segments.indexOf(segments.take(segments.size - 1).minBy { s -> s.second })
        val fastestPace = segments[fastestPaceIndex].second

        val summaryView: View = LayoutInflater.from(activity).inflate(R.layout.show_track_segments_info, null)
        UIUtils.addToLinearLayout(layout, summaryView, 25, 25, 25, 25)
        summaryView.findViewById<TextView>(R.id.tvValue1).text = Utils.paceToString(track.pace, false)
        summaryView.findViewById<TextView>(R.id.tvValue2).text = Utils.paceToString(fastestPace, false)

        for (i in segments.indices) {
            val segmentView: View = LayoutInflater.from(activity).inflate(R.layout.show_track_segment_view, null)
            UIUtils.addToLinearLayout(layout, segmentView, 25, 5, 25, 5)

            val guideline = segmentView.findViewById<Guideline>(R.id.guideline)

            if (i < segments.size - 1) {
                guideline.setGuidelinePercent((segments[i].second / maxValue).toFloat())
                segmentView.findViewById<TextView>(R.id.tvKM).text = Utils.distanceToStringShort(segments[i].first, 0)
                segmentView.findViewById<TextView>(R.id.tvValue).text = Utils.paceToString(segments[i].second, false)
            }
            else {
                guideline.setGuidelinePercent(0f)
                val finalSegmentDistance = if (i > 0) segments[i].first - segments[i - 1].first else segments[i].first
                segmentView.findViewById<TextView>(R.id.tvKM).text = "${Utils.distanceToString(finalSegmentDistance)}      ${Utils.paceToString(segments[i].second, false)}"
                segmentView.findViewById<TextView>(R.id.tvKM).setTextColor(Color.rgb(155,155,155))
                segmentView.findViewById<TextView>(R.id.tvValue).text = ""
            }
            segmentView.findViewById<ImageView>(R.id.ivValue).setColorFilter(
                    if (i == fastestPaceIndex)
                        Color.rgb(90, 144, 245)
                    else
                        Color.rgb(166, 255, 245)
            )
        }
    }

    private fun configureHeartrateSegments(layout: LinearLayout) {
        val segments = track.getHeartrateAtSegments().toList()
        if (segments.size < 2)
            return

        val maxValue = 200f
        val maxIndex = segments.indexOf(segments.take(segments.size - 1).maxBy { s -> s.second })
        val maxHeartrate = segments[maxIndex].second

        val summaryView: View = LayoutInflater.from(activity).inflate(R.layout.show_track_segments_info, null)
        UIUtils.addToLinearLayout(layout, summaryView, 25, 75, 25, 25)
        summaryView.findViewById<TextView>(R.id.tvTitle1).text = "Average heartrate"
        summaryView.findViewById<TextView>(R.id.tvValue1).text = track.averageHeartrate.toString()
        summaryView.findViewById<TextView>(R.id.tvTitle2).text = "Max heartrate"
        summaryView.findViewById<TextView>(R.id.tvValue2).text = maxHeartrate.toString()

        for (i in segments.indices) {
            val segmentView: View = LayoutInflater.from(activity).inflate(R.layout.show_track_segment_view, null)
            UIUtils.addToLinearLayout(layout, segmentView, 25, 5, 25, 5)

            val guideline = segmentView.findViewById<Guideline>(R.id.guideline)

            if (i < segments.size - 1) {
                guideline.setGuidelinePercent((segments[i].second / maxValue).toFloat())
                segmentView.findViewById<TextView>(R.id.tvKM).text = Utils.distanceToStringShort(segments[i].first, 0)
                segmentView.findViewById<TextView>(R.id.tvValue).text = segments[i].second.toString()
            }
            else {
                guideline.setGuidelinePercent(0f)
                val finalSegmentDistance = if (i > 1) segments[i].first - segments[i - 1].first else segments[i].first
                segmentView.findViewById<TextView>(R.id.tvKM).text = "${Utils.distanceToString(finalSegmentDistance)}      ${segments[i].second}"
                segmentView.findViewById<TextView>(R.id.tvKM).setTextColor(Color.rgb(155,155,155))
                segmentView.findViewById<TextView>(R.id.tvValue).text = ""
            }
            segmentView.findViewById<ImageView>(R.id.ivValue).setColorFilter(
                    if (i == maxIndex)
                        Color.rgb(90, 144, 245)
                    else
                        Color.rgb(166, 255, 245)
            )
        }
    }
}