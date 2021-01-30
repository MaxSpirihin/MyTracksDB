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

    enum class GroupType {
        MONTH,
        YEAR,
        //TODO WEEK,
        ALL_TIME;

        fun getName() : String {
            return when (this) {
                MONTH -> "By month"
                YEAR -> "By year"
                ALL_TIME -> "No grouping"
            }
        }
    }

    //region nested types
    class GroupItem(val groupName: String, val hided: Boolean)

    class GroupSummary(val isOneExerciseType: Boolean) {
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
            const val VIEW_TYPE_GROUP = 1
            const val VIEW_TYPE_GROUP_SUMMARY = 2
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

        class ViewHolderGroup(view: View, private val onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.tvMonth)
            val arrow: ImageView = view.findViewById(R.id.imageView)

            init {
                view.setOnClickListener{
                    onItemClicked(adapterPosition)
                }
            }
        }

        class ViewHolderGroupSummary(view: View) : RecyclerView.ViewHolder(view) {
            val titles = arrayOf(view.findViewById<TextView>(R.id.tvTitle1), view.findViewById(R.id.tvTitle2),view.findViewById(R.id.tvTitle3))
            val values = arrayOf(view.findViewById<TextView>(R.id.tvValue1), view.findViewById(R.id.tvValue2),view.findViewById(R.id.tvValue3))
        }

        override fun getItemViewType(position: Int): Int {
            return when {
                objects[position] is TrackDB -> VIEW_TYPE_ITEM
                objects[position] is GroupSummary -> VIEW_TYPE_GROUP_SUMMARY
                else -> VIEW_TYPE_GROUP
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_ITEM -> {
                    ViewHolderItem(LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.tracks_list_item, viewGroup, false), onItemClicked)
                }
                VIEW_TYPE_GROUP -> {
                    ViewHolderGroup(LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.tracks_list_item_month_name, viewGroup, false), onItemClicked)
                }
                VIEW_TYPE_GROUP_SUMMARY -> {
                    ViewHolderGroupSummary(LayoutInflater.from(viewGroup.context)
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
                VIEW_TYPE_GROUP -> {
                    val viewHolderGroup = viewHolder as ViewHolderGroup
                    val groupItem = objects[position] as GroupItem
                    viewHolderGroup.textView.text = groupItem.groupName
                    viewHolderGroup.arrow.rotation = if (groupItem.hided) 270f else 90f
                }
                VIEW_TYPE_GROUP_SUMMARY -> {
                    val viewHolderGroupSummary = viewHolder as ViewHolderGroupSummary
                    val descriptions = (objects[position] as GroupSummary).getDescriptions().toList()
                    for (i in viewHolderGroupSummary.titles.indices) {
                        viewHolderGroupSummary.titles[i].text = descriptions[i].first
                        viewHolderGroupSummary.values[i].text = descriptions[i].second
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
    private val mHidedGroups = hashSetOf<String>()
    private var mRecyclerAdapter : TracksListAdapter? = null
    private var mFilter = ExerciseType.UNKNOWN
    private var mGroupType = GroupType.values().first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)

        mAllTracks = TracksDatabase.loadAllTracks().sortedByDescending { track -> track.date }
        var types = ExerciseType.values().filter { t -> t == ExerciseType.UNKNOWN ||  mAllTracks!!.any { track -> track.exerciseType == t }}

        val spinnerTypesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types.map { t -> t.getName() })
        spinnerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerTypes = findViewById<View>(R.id.spinner) as Spinner
        spinnerTypes.adapter = spinnerTypesAdapter
        spinnerTypes.setSelection(0)
        spinnerTypes.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                mFilter = types[position]
                updateListObjects()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }

        val spinnerGroupsAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GroupType.values().map { t -> t.getName() })
        spinnerGroupsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerGroups = findViewById<View>(R.id.spinner_groups) as Spinner
        spinnerGroups.adapter = spinnerGroupsAdapter
        spinnerGroups.setSelection(0)
        spinnerGroups.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                mGroupType =  GroupType.values()[position]
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
            } else if (obj is GroupItem) {
                if (mHidedGroups.contains(obj.groupName))
                    mHidedGroups.remove(obj.groupName)
                else
                    mHidedGroups.add(obj.groupName)

                updateListObjects()
            }
        }
        recyclerView.adapter = mRecyclerAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun updateListObjects() {
        mListObjects.clear()
        var currentGroupName : String? = null
        var currentGroupIndex = 0
        var currentGroupSummary : GroupSummary? = null
        for (track in mAllTracks!!.filter { t -> mFilter == ExerciseType.UNKNOWN || t.exerciseType == mFilter }) {
            val groupName =
                    when (mGroupType) {
                        GroupType.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(track.date).toUpperCase()
                        GroupType.YEAR -> SimpleDateFormat("yyyy", Locale.ENGLISH).format(track.date).toUpperCase()
                        GroupType.ALL_TIME -> "All tracks"
                    }

            if (currentGroupName != groupName) {
                if (currentGroupSummary != null) {
                    mListObjects.add(currentGroupIndex, currentGroupSummary)
                    currentGroupSummary = null
                }

                mListObjects.add(GroupItem(groupName, mHidedGroups.contains(groupName)))
                currentGroupName = groupName
                currentGroupIndex = mListObjects.size
                if (!mHidedGroups.contains(groupName))
                    currentGroupSummary = GroupSummary(mFilter != ExerciseType.UNKNOWN)
            }

            if (!mHidedGroups.contains(groupName)) {
                mListObjects.add(track)
                currentGroupSummary?.addTrack(track)
            }

        }
        if (currentGroupSummary != null)
            mListObjects.add(currentGroupIndex, currentGroupSummary)

        mRecyclerAdapter?.notifyDataSetChanged()
    }
}