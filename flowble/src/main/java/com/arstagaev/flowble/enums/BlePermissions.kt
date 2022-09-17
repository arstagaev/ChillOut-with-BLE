package com.arstagaev.flowble.enums

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

enum class BlePermissions(var value: String) {

    BLUETOOTH_ON("on"),
    LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
    @RequiresApi(Build.VERSION_CODES.S)
    BLUETOOTH_CONNECT(Manifest.permission.BLUETOOTH_CONNECT),
    @RequiresApi(Build.VERSION_CODES.S)
    BLUETOOTH_SCAN(Manifest.permission.BLUETOOTH_SCAN),
}