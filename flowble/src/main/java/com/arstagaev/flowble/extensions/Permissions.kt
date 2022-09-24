package com.arstagaev.flowble.extensions

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.requestPermission(permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
}


////BLE
//fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
//    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)
//
//fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
//    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
//
//fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
//    properties and property != 0