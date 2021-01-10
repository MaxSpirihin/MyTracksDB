package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.core.db.TrackDB
import com.max.spirihin.mytracksdb.core.getIconId
import com.max.spirihin.mytracksdb.utilities.Utils
import com.max.spirihin.mytracksdb.utilities.toStringFormat
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*


class TracksListActivity : AppCompatActivity() {

    //region nested types
    class TrackItem(val track: TrackDB, val month: String)
    class MonthDescription(val month: String, val descriptions: Map<String, String>)

    class TracksListAdapter(private val objects: List<Any>, private val onItemClicked : (Int) -> Unit) :
            RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val VIEW_TYPE_ITEM = 0
            const val VIEW_TYPE_MONTH = 1
        }

        class ViewHolderItem(view: View, private val onItemClicked : (Int) -> Unit) : RecyclerView.ViewHolder(view) {
            val tvDistance: TextView = view.findViewById(R.id.tvDistance)
            val tvDuration: TextView = view.findViewById(R.id.tvDuration)
            val tvPace: TextView = view.findViewById(R.id.tvPace)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val image: ImageView = view.findViewById(R.id.ivExercityType)

            init {
                view.setOnClickListener{
                    onItemClicked(adapterPosition)
                }
            }
        }

        class ViewHolderMonth(view: View, private val onItemClicked : (Int) -> Unit) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.tvMonth)

            init {
                view.setOnClickListener{
                    onItemClicked(adapterPosition)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (objects[position] is TrackItem) VIEW_TYPE_ITEM else VIEW_TYPE_MONTH
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_ITEM -> {
                    ViewHolderItem(LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.tracks_list_item, viewGroup, false), onItemClicked)
                }
                VIEW_TYPE_MONTH -> {
                    ViewHolderMonth(LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.tracks_list_item_month_name, viewGroup, false), onItemClicked)
                }
                else -> throw NullPointerException()
            }
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

            when (viewHolder.itemViewType) {
                VIEW_TYPE_ITEM -> {
                    val viewHolderItem = viewHolder as ViewHolderItem
                    val track = (objects[position] as TrackItem).track
                    val distanceInKM = track.distance / 1000.0
                    viewHolderItem.tvDistance.text = "%.2f".format(distanceInKM) + " km"
                    viewHolderItem.tvDuration.text = Utils.secondsToString(track.duration)
                    viewHolderItem.tvPace.text = Utils.paceToString(track.pace)
                    viewHolderItem.tvDate.text = track.date.toStringFormat("dd.MM")

                    viewHolderItem.image.setImageResource(track.exerciseType.getIconId())
                }
                VIEW_TYPE_MONTH -> {
                    val viewHolderMonth = viewHolder as ViewHolderMonth
                    viewHolderMonth.textView.text = objects[position].toString()
                }
                else -> {}
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = objects.size
    }

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTracks)

        val tracks = TracksDatabase.loadAllTracks().sortedByDescending { track -> track.date }

        val hidedMonths = mutableListOf<String>()
        val hidedTracks = mutableListOf<TrackItem>()

        val objects = mutableListOf<Any>()
        var currentMonth : String? = null
        for (track in tracks) {
            val month =  SimpleDateFormat("MMMM YYYY", Locale.ENGLISH).format(track.date).toUpperCase()
            if (currentMonth != month) {
                objects.add(month)
                currentMonth = month
            }
            objects.add(TrackItem(track, month))
        }

        var adapter : TracksListAdapter? = null
        adapter = TracksListAdapter(objects) { pos ->
            val obj = objects[pos]
            if (obj is TrackItem) {
                val intent = Intent(this, ShowTrackActivity::class.java)
                intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, obj.track.id)
                startActivity(intent)
            } else if (obj is String) {
                if (hidedMonths.contains(obj)) {
                    hidedMonths.remove(obj)
                    val tracks = hidedTracks.filter { t -> t.month == obj }
                    objects.addAll(objects.indexOf(obj) + 1, tracks)
                    hidedTracks.removeAll(tracks)
                } else {
                    hidedMonths.add(obj)
                    val tracks = objects.filter { t -> t is TrackItem && t.month == obj }.map { t -> t as TrackItem }
                    hidedTracks.addAll(tracks)
                    objects.removeAll(tracks)
                }
                adapter?.notifyDataSetChanged()
            }
        }
        recyclerView.adapter = adapter
    }
}