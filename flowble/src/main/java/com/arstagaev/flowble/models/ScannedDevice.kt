package com.arstagaev.flowble.models

import android.bluetooth.BluetoothDevice

data class ScannedDevice(
    var bt   : BluetoothDevice,
    var rssi : Int
)
