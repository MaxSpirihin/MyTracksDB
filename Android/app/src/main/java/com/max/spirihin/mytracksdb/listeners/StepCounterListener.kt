package com.max.spirihin.mytracksdb.listeners

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.max.spirihin.mytracksdb.utilities.Print

class StepCounterListener : SensorEventListener {
    private var startStepsCount : Int? = null
    var stepsCount : Int = 0
        private set
    var sensorManager : SensorManager? = null
    var sensor : Sensor? = null

    fun startListen(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager == null)
        {
            Print.LogError("[StepCounterListener] SensorManager is null")
            return
        }

        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null)
        {
            Print.LogError("[StepCounterListener] sensor is null")
            return
        }

        sensorManager?.registerListener(this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListen() {
        if (sensorManager != null && sensor != null)
            sensorManager?.unregisterListener(this, sensor)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        val newSteps = (sensorEvent?.values?.getOrNull(0) ?: 0.0f).toInt()
        Print.Log("[StepCounterListener] onSensorChanged ${sensorEvent?.sensor?.name} $newSteps ")

        if (startStepsCount == null)
            startStepsCount = newSteps

        stepsCount = newSteps - startStepsCount!!
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Print.Log("[StepCounterListener] onAccuracyChanged ${sensor?.name} $accuracy")
    }
}