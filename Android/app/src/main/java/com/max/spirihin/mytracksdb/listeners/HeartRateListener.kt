package com.max.spirihin.mytracksdb.listeners

import android.content.Context
import android.widget.Toast
import com.max.spirihin.mytracksdb.utilities.Preferences
import com.max.spirihin.mytracksdb.utilities.Print
import com.wahoofitness.connector.HardwareConnector
import com.wahoofitness.connector.HardwareConnectorEnums
import com.wahoofitness.connector.HardwareConnectorTypes
import com.wahoofitness.connector.capabilities.Capability
import com.wahoofitness.connector.capabilities.Heartrate
import com.wahoofitness.connector.conn.connections.SensorConnection
import com.wahoofitness.connector.conn.connections.params.ConnectionParams
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener

//TODO Support cache connectionParams and break connection
class HeartRateListener : HardwareConnector.Listener, DiscoveryListener, SensorConnection.Listener, Heartrate.Listener {
    var currentHeartrate : Int = 0
        private set
    private var hardwareConnector : HardwareConnector? = null
    private var context : Context? = null

    fun startListen(context: Context) {
        this.context = context
        hardwareConnector = HardwareConnector(context, this)
        val seriaized = Preferences.getString("HeartRateConnectionParams", "")
        val connectionParams = ConnectionParams.deserialize(seriaized)
        if (connectionParams != null)
            connect(connectionParams)
        else
            hardwareConnector!!.startDiscovery(this)
    }

    fun stopListen() {
        hardwareConnector!!.shutdown()
    }

    private fun log(log: String) {
        Print.Log("[HeartRateListener] $log")
    }

    override fun onHardwareConnectorStateChanged(networkType: HardwareConnectorTypes.NetworkType, connectorState: HardwareConnectorEnums.HardwareConnectorState) {
        log("onHardwareConnectorStateChanged $networkType $connectorState")
    }

    override fun onFirmwareUpdateRequired(connectionState: SensorConnection, p1: String, p2: String) {
        log("onFirmwareUpdateRequired")
    }

    override fun onDeviceDiscovered(connectionParams: ConnectionParams) {
        log("onDeviceDiscovered ${connectionParams.name}")
        if (connectionParams.sensorType != HardwareConnectorTypes.SensorType.HEARTRATE)
            return

        val serialized = connectionParams.serialize()
        if (!serialized.isNullOrBlank())
            Preferences.setString("HeartRateConnectionParams", serialized)

        hardwareConnector!!.stopDiscovery(this)
        connect(connectionParams)
    }

    private fun connect(connectionParams: ConnectionParams) {
        Toast.makeText(context, "start connect to heartrate monitor", Toast.LENGTH_LONG).show()
        hardwareConnector!!.requestSensorConnection(connectionParams, this)
    }

    override fun onDiscoveredDeviceLost(connectionParams: ConnectionParams) {
        Toast.makeText(context, "cant find heartrate monitor", Toast.LENGTH_SHORT).show()
        hardwareConnector!!.stopDiscovery(this)
        log("onDiscoveredDeviceLost ${connectionParams.name}")
    }

    override fun onDiscoveredDeviceRssiChanged(connectionParams: ConnectionParams, p1: Int) {
        log("onDiscoveredDeviceRssiChanged ${connectionParams.name} $p1")
    }

    override fun onNewCapabilityDetected(sensorConnection: SensorConnection, capabilityType: Capability.CapabilityType) {
        log("onNewCapabilityDetected ${sensorConnection.connectionParams.name} $capabilityType")
        if (capabilityType === Capability.CapabilityType.Heartrate) {
            Toast.makeText(context, "heartrate monitor connected", Toast.LENGTH_LONG).show()
            val heartrate = sensorConnection.getCurrentCapability(Capability.CapabilityType.Heartrate) as Heartrate?
            heartrate!!.addListener(this)
            updateHeartrate(heartrate.heartrateData)
        }
    }

    private fun updateHeartrate(heartrateData: Heartrate.Data) {
        currentHeartrate = heartrateData.heartrate.asEventsPerMinute().toInt()
    }

    override fun onSensorConnectionStateChanged(p0: SensorConnection, p1: HardwareConnectorEnums.SensorConnectionState) {
        log("onSensorConnectionStateChanged ${p0.connectionParams.name} ${p1.name}")
    }

    override fun onSensorConnectionError(p0: SensorConnection, p1: HardwareConnectorEnums.SensorConnectionError) {
        log("onSensorConnectionError ${p0.connectionParams.name} $p1")
        Toast.makeText(context, "heartrate monitor connection lost", Toast.LENGTH_LONG).show()
        currentHeartrate = 0
    }

    override fun onHeartrateData(p0: Heartrate.Data) {
        log("onHeartrateData ${p0.heartrate}")
        updateHeartrate(p0)
    }

    override fun onHeartrateDataReset() {
        log("onHeartrateDataReset")
    }
}