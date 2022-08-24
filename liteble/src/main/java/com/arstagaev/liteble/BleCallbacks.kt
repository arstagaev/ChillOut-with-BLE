package com.arstagaev.liteble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.arstagaev.liteble.BleParameters.GATT_SERVICES
import com.arstagaev.liteble.BleParameters.operationQueue
import com.arstagaev.liteble.gentelman_kit.bytesToHex
import com.arstagaev.liteble.models.ScannedDevice
import com.arstagaev.liteble.models.StateBle




var leScanCallback = object : ScanCallback() {
    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

    }


    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        Log.i("www","www ${BleParameters.scanResults.value.joinToString()}  || [${result.device.address}]")
        //Log.i(TAG,"onScanResult: ${result.rssi}")

        val indexQuery = BleParameters.scanResults.replayCache.last().indexOfFirst {
            it.bt.address == result.device.address
        }
        /** A scan result already exists with the same address */
        if (indexQuery != -1) {
            // for closest connect
            // refreshing is about 20-60 sec rssi
            for (i in 0 until BleParameters.scanResults.replayCache.last().size) {
                if (BleParameters.scanResults.replayCache.last()[i].bt.address == result.device.address) {
                    BleParameters.scanResults.value[i].rssi = result.rssi

                }
            }



        } else { /** founded new device */
            BleParameters.scanResults.value.add(ScannedDevice(result.device,result.rssi))
            BleParameters.scanResults.value.sortByDescending { it.rssi } // sort by rssi found devices
            //Log.i(TAGx,"Found BLE device!!! Name: ${result.device.name ?: "Unnamed"}, address: ${result.device.address}")
        }
    }
}