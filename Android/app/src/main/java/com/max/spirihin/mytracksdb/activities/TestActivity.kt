package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.core.db.TrackDB
import com.max.spirihin.mytracksdb.utilities.Print
import java.text.SimpleDateFormat
import java.util.*


class TestActivity : AppCompatActivity() {

    class TrackItem(val track: TrackDB, val month: String)
    class MonthDescription(val month: String, val descriptions: Map<String, String>)

    private var textView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter_test)

        textView = findViewById<TextView>(R.id.tvTest)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTracks)

        val tracks = TracksDatabase.loadAllTracks().sortedByDescending { track -> track.date }

        val hidedMonths = mutableListOf<String>()
        val hidedTracks = mutableListOf<TrackItem>()

        val objects = mutableListOf<Any>()
        var currentMonth : String? = null
        for (track in tracks) {
            val month =  SimpleDateFormat("MMMM", Locale.ENGLISH).format(track.date).toUpperCase()
            if (currentMonth != month) {
                objects.add(month)
                currentMonth = month
            }
            objects.add(TrackItem(track, month))
        }

        var adapter : CustomAdapter? = null
        adapter = CustomAdapter(objects) { pos ->
            val obj = objects[pos]
            if (obj is TrackItem) {
                val intent = Intent(this, ShowTrackActivity::class.java)
                intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, obj.track.id)
                startActivity(intent)
            } else if (obj is String) {
                val month = obj
                if (hidedMonths.contains(month)) {
                    hidedMonths.remove(month)
                    val tracks = hidedTracks.filter { t -> t.month == month }
                    objects.addAll(objects.indexOf(month) + 1, tracks)
                    hidedTracks.removeAll(tracks)
                } else {
                    hidedMonths.add(month)
                    val tracks = objects.filter { t -> t is TrackItem && t.month == month }.map { t -> t as TrackItem }
                    hidedTracks.addAll(tracks)
                    objects.removeAll(tracks)
                }
                adapter?.notifyDataSetChanged()
            }
        }
        recyclerView.adapter = adapter

       // Timer().schedule(1000,1000) {
       //     runOnUiThread {
       //         tracksNames.removeAt(0)
       //         adapter.notifyDataSetChanged()
       //     }
       // }
    }

}

class CustomAdapter(private val objects: List<Any>, private val onItemClicked : (Int) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolderItem(view: View, private val onItemClicked : (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.tvDistance)
            view.setOnClickListener{
                onItemClicked(getAdapterPosition())
            }
        }
    }

    class ViewHolderMonth(view: View, private val onItemClicked : (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.tvMonth)
            view.setOnClickListener{
                onItemClicked(getAdapterPosition())
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (objects[position] is TestActivity.TrackItem) 0 else 1
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                ViewHolderItem(LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.tracks_list_item, viewGroup, false), onItemClicked)
            }
            else -> {
                ViewHolderMonth(LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.tracks_list_item_month_name, viewGroup, false), onItemClicked)
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        when (viewHolder.getItemViewType()) {
            0 -> {
                val viewHolderItem = viewHolder as ViewHolderItem
                val distanceInKM = (objects[position] as TestActivity.TrackItem).track.distance / 1000.0
                viewHolderItem.textView.text = "%.2f".format(distanceInKM) + " km"
            }
            1 -> {
                val viewHolderMonth = viewHolder as ViewHolderMonth
                viewHolderMonth.textView.text = objects[position].toString()
            }
            else -> {}
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = objects.size

}