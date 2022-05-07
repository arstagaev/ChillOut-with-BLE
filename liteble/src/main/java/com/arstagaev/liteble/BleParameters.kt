package com.arstagaev.liteble

import android.bluetooth.le.ScanResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object BleParameters {
    var scanResults = MutableSharedFlow<ArrayList<ScanResult>>(0,0,BufferOverflow.SUSPEND)

    var CHARACTERISTIC_UUID_1 = "zero" // need to setup
    var CHARACTERISTIC_UUID_2 = "zero" // need to setup
    var CCC_DESCRIPTOR_UUID   = "zero" // need to setup

}