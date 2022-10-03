package com.arstagaev.flowble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanFilter
import com.arstagaev.flowble.models.ScannedDevice
import com.arstagaev.flowble.models.StateBle
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import kotlin.collections.ArrayList

object BleParameters {
    var STATE_BLE = StateBle.INIT

    var scanResultsX  = MutableStateFlow<ArrayList<ScannedDevice>>(arrayListOf())
    var SCAN_FILTERS = mutableListOf<ScanFilter>()
    var GATT_SERVICES :  List<BluetoothGattService>? = null

    var TARGET_CHARACTERISTIC_NOTIFY : UUID? = null


    const val ACTION_GATT_CONNECTED           = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED        = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

    var CONNECTED_DEVICE : BluetoothDevice? = null
    var BLE_STATUS = -1

    var BLE_BATTERY_LEVEL_CHARACTERISTIC = "00002a19-0000-1000-8000-00805f9b34fb"
    var BLE_BATTERY_VALUE = "-1"



}