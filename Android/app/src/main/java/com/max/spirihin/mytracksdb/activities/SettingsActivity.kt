package com.max.spirihin.mytracksdb.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.max.spirihin.mytracksdb.Helpers.SpeakerInfoType
import com.max.spirihin.mytracksdb.Helpers.SpeakerPreferences
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.ExerciseType
import com.max.spirihin.mytracksdb.core.TracksDatabase

class SettingsActivity : AppCompatActivity() {

    var mLinearLayout : LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mLinearLayout = findViewById(R.id.linearLayout)

        addButton("Update database") {
            AlertDialog.Builder(this)
                    .setTitle("Update database")
                    .setMessage("Do you really want update database?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.updateDatabase()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        addButton("Backup database") {
            AlertDialog.Builder(this)
                    .setTitle("Backup database")
                    .setMessage("Do you really want backup database?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.backupSqlFile()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        addButton("Restore database") {
            AlertDialog.Builder(this)
                    .setTitle("STOP. WARNING!!!")
                    .setMessage("Do you really want restore database? You must be absolutely sure. Restore is making from \"for_restore\" " +
                            "file in MyTracksDB folder. Prepare it manually. Before restore extra backup will be made")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        TracksDatabase.restoreSqlFile()
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
        }

        addIntField("GPS update time (in seconds)", Preferences.gpsUpdateSeconds) { value -> Preferences.gpsUpdateSeconds = value }
        addIntField("GPS update distance (in meters)", Preferences.gpsUpdateMeters) { value -> Preferences.gpsUpdateMeters = value }
        addIntField("Max allowed gps mistake (in meters)", Preferences.gpsMaxAccuracy) { value -> Preferences.gpsMaxAccuracy = value }

        addSpeakerSettings()
    }

    private fun addSpeakerSettings() {
        val exerciseTypes = ExerciseType.values().filter { e -> e != ExerciseType.UNKNOWN }
        var currentExerciseType : ExerciseType = ExerciseType.EASY_RUN

        val checkBoxes = mutableMapOf<SpeakerInfoType, CheckBox>()
        var distanceField : EditText? = null

        fun updateValues() {
            for (checkBox in checkBoxes) {
                checkBox.value.isChecked = SpeakerPreferences.getNeedSpeakType(currentExerciseType, checkBox.key)
            }

            distanceField?.setText(SpeakerPreferences.getSpeakDistance(currentExerciseType).toString())
        }

        addSpinner(exerciseTypes.map { v -> v.toString() }, exerciseTypes.indexOf(currentExerciseType)) { pos ->
            currentExerciseType = exerciseTypes[pos]
            updateValues()
        }

        distanceField = addIntField("Speak after distance (in meters)", 0) { value ->
            SpeakerPreferences.setSpeakDistance(currentExerciseType, value)
        }

        for (speakerInfoType in SpeakerInfoType.values()) {
            val checkBox = addCheckbox(speakerInfoType.getString(), false) { checked ->
                SpeakerPreferences.setNeedSpeakType(currentExerciseType, speakerInfoType, checked)
            }
            checkBoxes[speakerInfoType] = checkBox
        }
        updateValues()
    }

    private fun addIntField(text: String, startValue: Int, onChange: (Int) -> Unit) : EditText {
        val textView = TextView(this)
        textView.text = text
        addToLinearLayout(textView, 50)

        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.setText(startValue.toString())
        editText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank())
                onChange?.invoke(text.toString().toInt())
        }
        addToLinearLayout(editText, 150)
        return editText
    }

    private fun addButton(text: String, onClick: () -> Unit) {
        val button = Button(this)
        button.text = text
        button.setOnClickListener { onClick?.invoke() }
        addToLinearLayout(button, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun addSpinner(values: List<String>, defaultPosition: Int, onChange: (Int) -> Unit) {
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = Spinner(this)
        spinner.adapter = spinnerAdapter
        spinner.setSelection(defaultPosition)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View,
                                        position: Int, id: Long) {
                onChange?.invoke(position)
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }

        addToLinearLayout(spinner, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun addCheckbox(text: String, default: Boolean, onChange: (Boolean) -> Unit) : CheckBox {
        val checkBox = CheckBox(this)
        checkBox.text = text
        checkBox.isChecked = default
        checkBox.setOnCheckedChangeListener { _, checked -> onChange?.invoke(checked) }
        addToLinearLayout(checkBox, LinearLayout.LayoutParams.WRAP_CONTENT)
        return checkBox
    }

    private fun addToLinearLayout(view: View, height: Int) {
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        )
        view.layoutParams = lp
        mLinearLayout!!.addView(view)
    }
}