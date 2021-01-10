package com.max.spirihin.mytracksdb.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.max.spirihin.mytracksdb.R
import com.wahoofitness.connector.HardwareConnector
import com.wahoofitness.connector.HardwareConnectorEnums
import com.wahoofitness.connector.HardwareConnectorTypes
import com.wahoofitness.connector.capabilities.Capability
import com.wahoofitness.connector.capabilities.Capability.CapabilityType
import com.wahoofitness.connector.capabilities.Heartrate
import com.wahoofitness.connector.conn.connections.SensorConnection
import com.wahoofitness.connector.conn.connections.params.ConnectionParams
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener


class StepCounterTestActivity : AppCompatActivity(), HardwareConnector.Listener, DiscoveryListener, SensorConnection.Listener, Heartrate.Listener {

    private var textView : TextView? = null

    private var hardwareConnector : HardwareConnector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracks_list)


        textView = findViewById<TextView>(R.id.tvTest)

        hardwareConnector = HardwareConnector(this, this)
        hardwareConnector!!.startDiscovery(this);
    }

    override fun onDestroy() {
        hardwareConnector!!.shutdown();
        super.onDestroy()
    }

    override fun onHardwareConnectorStateChanged(p0: HardwareConnectorTypes.NetworkType, p1: HardwareConnectorEnums.HardwareConnectorState) {
    }

    override fun onFirmwareUpdateRequired(p0: SensorConnection, p1: String, p2: String) {

    }

    override fun onDeviceDiscovered(p0: ConnectionParams) {
        textView!!.text = "${textView!!.text}\nonDeviceDiscovered ${p0.name}"
        hardwareConnector!!.requestSensorConnection(p0, this)
    }

    override fun onDiscoveredDeviceLost(p0: ConnectionParams) {

    }

    override fun onDiscoveredDeviceRssiChanged(p0: ConnectionParams, p1: Int) {

    }

    override fun onNewCapabilityDetected(sensorConnection: SensorConnection, capabilityType: Capability.CapabilityType) {
        if (capabilityType === CapabilityType.Heartrate) {
            val heartrate = sensorConnection.getCurrentCapability(CapabilityType.Heartrate) as Heartrate?
            heartrate!!.addListener(this)
        }

    }

    fun getHeartrateData(sensorConnection: SensorConnection): Heartrate.Data? {
        return if (sensorConnection != null) {
            val heartrate = sensorConnection
                    .getCurrentCapability(CapabilityType.Heartrate) as Heartrate
            heartrate?.heartrateData
        } else {
            // Sensor not connected
            null
        }
    }

    override fun onSensorConnectionStateChanged(p0: SensorConnection, p1: HardwareConnectorEnums.SensorConnectionState) {
        textView!!.text = "${textView!!.text}\nonSensorConnectionStateChanged ${p0.connectionParams.name} ${p1.name}"
    }

    override fun onSensorConnectionError(p0: SensorConnection, p1: HardwareConnectorEnums.SensorConnectionError) {

    }

    override fun onHeartrateData(p0: Heartrate.Data) {
        textView!!.text = "${textView!!.text}\nonSensorConnectionStateChanged ${p0.heartrate} ${p0.accumulatedBeats}"
    }

    override fun onHeartrateDataReset() {
    }
}