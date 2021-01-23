package com.max.spirihin.mytracksdb.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.max.spirihin.mytracksdb.core.Track


class ShowTrackPageFragmentAdapter(fm: FragmentManager, context: Context, val track: Track) : FragmentPagerAdapter(fm) {

    private val tabs = ShowTrackTab.values()
    private val context: Context = context

    override fun getCount(): Int {
        return tabs.size
    }

    override fun getItem(position: Int): Fragment {
        return when (tabs[position]) {
            ShowTrackTab.TRACK -> ShowTrackFragmentTrack(track)
            ShowTrackTab.PACE -> PageFragment.newInstance(position + 1)
            ShowTrackTab.SUMMARY -> PageFragment.newInstance(position + 1)
            ShowTrackTab.INFO -> ShowTrackFragmentInfo(track)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (tabs[position]) {
            ShowTrackTab.TRACK -> "Track"
            ShowTrackTab.PACE -> "Pace"
            ShowTrackTab.SUMMARY -> "Summary"
            ShowTrackTab.INFO -> "Info"
        }
    }

}