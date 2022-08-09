package com.arstagaev.liteble

import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback

interface Absd {
    var bluetoothGattCallback: BluetoothGattCallback?
    var bluetoothLeScanner: BluetoothLeScanner?
    var leScanCallback: ScanCallback?
}