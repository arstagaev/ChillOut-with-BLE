package com.arstagaev.liteble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import com.arstagaev.liteble.models.ScannedDevice
import com.arstagaev.liteble.models.StateBle
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

object BleParameters {
    var STATE_BLE = StateBle.INIT

    var scanResults  = MutableStateFlow<ArrayList<ScannedDevice>>(arrayListOf())
    var SCAN_FILTERS = mutableListOf<ScanFilter>()
    var GATT_SERVICES :  List<BluetoothGattService>? = null

    var operationQueue = ArrayList<BleOperations>()

    var TARGET_CHARACTERISTIC : BluetoothGattCharacteristic? = null

    var CHARACTERISTIC_UUID_1 = "zero" // need to setup
    var CHARACTERISTIC_UUID_2 = "zero" // need to setup
    var CCC_DESCRIPTOR_UUID   = "zero" // need to setup

    const val ACTION_GATT_CONNECTED           = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED        = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

    var CONNECTED_DEVICE : BluetoothDevice? = null
    var BLE_STATUS = -1
}