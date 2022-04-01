package com.arstagaev.chilloutble.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.arstagaev.backble.ble.enums.StateOfService
import com.arstagaev.backble.ble.enums.StyleOfConnect
import com.arstagaev.backble.ble.models.ScannedDevice
import com.arstagaev.chilloutble.ble.control_module.enum.ActionOfService

object BLEParameters {
    init {
        Log.w("TAG","inSCAN_FILTERS!!!!!!!!!!!!!!!!!!")

    }

    var STATE_NOW_SERVICE        = StateOfService.INIT
    var ACTION_NOW_SERVICE       = ActionOfService.START
    var BLE_STATUS = -1

    var CURRENT_SERVICE_CONNECT_STYLE= StyleOfConnect.TARGET // need to setup

    var TRACKED_NAME_OF_BLE_DEVICE : String?  = null
    var TRACKED_BLE_DEVICE  : ScannedDevice?  = null
    var CONNECTED_DEVICE  : BluetoothDevice?  = null

    var SCAN_PERIOD          = 10_000L  // need to setup
    var SCAN_PERIOD_ADV      = 20_000L  // need to setup
    var WITH_NOTIFY          = false   // need to setup
    var TRACKING_ADVERTISING = false   // need to setup
    var isADVERTISING_WORK   = true    // need to setup
    //var WITH_SCAN_FILTER     = true
    //var NEED_RESET           = false   // need to setup

    const val ACTION_GATT_CONNECTED           = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED        = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

    var CHARACTERISTIC_UUID_1 = "zero" // need to setup
    var CHARACTERISTIC_UUID_2 = "zero" // need to setup
    var CCC_DESCRIPTOR_UUID   = "zero" // need to setup
    var TARGET_CHARACTERISTIC : BluetoothGattCharacteristic? = null
    var TARGET_PART_OF_NAME   : String = "zero"

    var TARGET_CONNECT_ADDRESS = ""
    var SUPER_BLE_DEVICE : BluetoothDevice? = null // deprecated
    var scanResultsX           = mutableStateListOf<ScannedDevice>()
    var SCAN_FILTERS           = mutableListOf<ScanFilter>()

    const val SEND_TO_UNBOND = "01" // need to setup

    // drafts
    const val SAMPLE_ADDRESS_1 = "84:2E:14:9F:2F:D3" //"80:EA:CA:70:AE:FA" new  // need to setup
    //const val SAMPLE_ADDRESS_1 = "80:EA:CA:70:AE:FA" // old   // need to setup


    //for debug
    var MSG = ""
}