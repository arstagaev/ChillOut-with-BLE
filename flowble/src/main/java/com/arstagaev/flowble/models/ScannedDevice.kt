package com.arstagaev.liteble.models

import android.bluetooth.BluetoothDevice

data class ScannedDevice(
    var bt   : BluetoothDevice,
    var rssi : Int
)
