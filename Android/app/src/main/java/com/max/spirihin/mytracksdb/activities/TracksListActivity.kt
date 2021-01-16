package com.max.spirihin.mytracksdb.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.core.TracksDatabase
import com.max.spirihin.mytracksdb.core.db.TrackDB
import com.max.spirihin.mytracksdb.core.getIconId
import com.max.spirihin.mytracksdb.core.getName
import com.max.spirihin.mytracksdb.utilities.Utils
import com.max.spirihin.mytracksdb.utilities.toStringFormat
import java.text.SimpleDateFormat
import java.util.*


class TracksListActivity : AppCompatActivity() {

    //region nested types
    class MonthItem(val month: String, val hided: Boolean)

    class MonthSummary(val isOneExerciseType: Boolean) {
        private val tracks = mutableListOf<TrackDB>()

        fun getDescriptions(): Map<String, String> {
           return if (isOneExerciseType) getDescriptionsOneExerciseType() else getDescriptionsAllExercises()
        }

        private fun getDescriptionsOneExerciseType(): Map<String, String> {
            return mapOf(
                    "Distance (km)" to Utils.distanceToStringShort(tracks.sumBy { track -> track.distance }),
                    "Duration" to Utils.secondsToString(tracks.sumBy { track -> track.duration }),
                    "Count" to tracks.count().toString()
            )
        }

        private fun getDescriptionsAllExercises(): Map<String, String> {
            val result = mutableMapOf<String, String>()

            //define priority types
            var types : MutableList<ExerciseType> = mutableListOf(ExerciseType.EASY_RUN, ExerciseType.BICYCLE, ExerciseType.SKIING, ExerciseType.SKATES, ExerciseType.WALKING)

            //add other types
            for (type in ExerciseType.values()){
                if (!types.contains(type))
                    types.add(type)
            }

            types = types.sortedBy { t -> if (tracks.any { track -> track.exerciseType == t }) 0 else 1 }.toMutableList()

            for (i in 0..2) {
                val distance = tracks.filter { track -> track.exerciseType == types[i] }.sumBy { track -> track.distance }
                result[types[i].getName() + " (km)"] =
                        Utils.distanceToStringShort(distance)
            }
            return result
        }

        fun addTrack(track: TrackDB) {
            tracks.add(track)
        }
    }

    class TracksListAdapter(private val objects: List<Any>, private val onItemClicked: (Int) -> Unit) :
            RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val VIEW_TYPE_ITEM = 0
            const val VIEW_TYPE_MONTH = 1
            const val VIEW_TYPE_MONTH_SUMMARY = 2
        }

        class ViewHolderItem(view: View, private val onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
            val tvDistance: TextView = view.findViewById(R.id.text)
            val tvDuration: TextView = view.findViewById(R.id.tvDuration)
            val tvPace: TextView = view.findViewById(R.id.tvPace)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val image: ImageView = view.findViewById(R.id.bigIcon)

            init {
                view.setOnClickListener{
                    onItemClicked(adapterPosition)
                }
            }
        }

        class ViewHolderMonth(view: View, private val onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.tvMonth)
            val arrow: ImageView = view.findViewById(R.id.imageView)

            init {
                view.setOnClickListener{
                    onItemClicked(adapterPosition)
                }
            }
        }

        class ViewHolderMonthSummary(view: View) : RecyclerView.ViewHolder(view) {
            val titles = arrayOf(view.findViewById<TextView>(R.id.tvTitle1), view.findViewById(R.id.tvTitle2),view.findViewById(R.id.tvTitle3))
            val values = arrayOf(view.findViewById<TextView>(R.id.tvValue1), view.findViewById(R.id.tvValue2),view.findViewById(R.id.tvValue3))
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                objects[position] is TrackDB -> VIEW_TYPE_ITEM
                objects[position] is MonthSummary -> VIEW_TYPE_MONTH_SUMMARY
                else -> VIEW_TYPE_MONTH
            }
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
                VIEW_TYPE_MONTH_SUMMARY -> {
                    ViewHolderMonthSummary(LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.tracks_list_item_month_summary, viewGroup, false))
                }
                else -> throw NullPointerException()
            }
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            when (viewHolder.itemViewType) {
                VIEW_TYPE_ITEM -> {
                    val viewHolderItem = viewHolder as ViewHolderItem
                    val track = objects[position] as TrackDB
                    viewHolderItem.tvDistance.text = Utils.distanceToString(track.distance)
                    viewHolderItem.tvDuration.text = Utils.secondsToString(track.duration)
                    viewHolderItem.tvPace.text = Utils.paceToString(track.pace)
                    viewHolderItem.tvDate.text = track.date.toStringFormat("dd.MM")

                    viewHolderItem.image.setImageResource(track.exerciseType.getIconId())
                }
                VIEW_TYPE_MONTH -> {
                    val viewHolderMonth = viewHolder as ViewHolderMonth
                    val monthItem = objects[position] as MonthItem
                    viewHolderMonth.textView.text = monthItem.month.toString()
                    viewHolderMonth.arrow.rotation = if (monthItem.hided) 270f else 90f
                }
                VIEW_TYPE_MONTH_SUMMARY -> {
                    val viewHolderMonthSummary = viewHolder as ViewHolderMonthSummary
                    val descriptions = (objects[position] as MonthSummary).getDescriptions().toList()
                    for (i in viewHolderMonthSummary.titles.indices) {
                        viewHolderMonthSummary.titles[i].text = descriptions[i].first
                        viewHolderMonthSummary.values[i].text = descriptions[i].second
                    }
                }
                else -> {}
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = objects.size
    }

    //endregion

    private var mAllTracks : List<TrackDB>? = null
    private val mListObjects = mutableListOf<Any>()
    private val mHidedMonths = hashSetOf<String>()
    private var mRecyclerAdapter : TracksListAdapter? = null
    private var mFilter = ExerciseType.UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)

        mAllTracks = TracksDatabase.loadAllTracks().sortedByDescending { track -> track.date }
        var types = ExerciseType.values().filter { t -> t == ExerciseType.UNKNOWN ||  mAllTracks!!.any { track -> track.exerciseType == t }}

        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types.map { t -> t.getName() })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = findViewById<View>(R.id.spinner) as Spinner
        spinner.adapter = spinnerAdapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                mFilter = types[position]
                updateListObjects()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvTracks)
        mRecyclerAdapter = TracksListAdapter(mListObjects) { pos ->
            val obj = mListObjects[pos]
            if (obj is TrackDB) {
                val intent = Intent(this, ShowTrackActivity::class.java)
                intent.putExtra(ShowTrackActivity.TRACK_ID_INTENT_STRING, obj.id)
                startActivity(intent)
            } else if (obj is MonthItem) {
                if (mHidedMonths.contains(obj.month))
                    mHidedMonths.remove(obj.month)
                else
                    mHidedMonths.add(obj.month)

                updateListObjects()
            }
        }
        recyclerView.adapter = mRecyclerAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun updateListObjects() {
        mListObjects.clear()
        var currentMonth : String? = null
        var currentMonthIndex = 0
        var currentMonthSummary : MonthSummary? = null
        for (track in mAllTracks!!.filter { t -> mFilter == ExerciseType.UNKNOWN || t.exerciseType == mFilter }) {
            val month =  SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(track.date).toUpperCase()
            if (currentMonth != month) {
                if (currentMonthSummary != null) {
                    mListObjects.add(currentMonthIndex, currentMonthSummary)
                    currentMonthSummary = null
                }

                mListObjects.add(MonthItem(month, mHidedMonths.contains(month)))
                currentMonth = month
                currentMonthIndex = mListObjects.size
                if (!mHidedMonths.contains(month))
                    currentMonthSummary = MonthSummary(mFilter != ExerciseType.UNKNOWN)
            }

            if (!mHidedMonths.contains(month)) {
                mListObjects.add(track)
                currentMonthSummary?.addTrack(track)
            }

        }
        if (currentMonthSummary != null)
            mListObjects.add(currentMonthIndex, currentMonthSummary)

        mRecyclerAdapter?.notifyDataSetChanged()
    }
}