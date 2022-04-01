package com.arstagaev.backble.ble.models

import android.bluetooth.BluetoothDevice

data class ScannedDevice(
    var bt  : BluetoothDevice,
    var rssi:Int
)
