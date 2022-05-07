package com.arstagaev.liteble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.core.content.getSystemService
import com.arstagaev.liteble.BleParameters.scanResults

abstract class BleManager(ctx : Context)  {

    protected val TAG = BleManager::class.qualifiedName
    internal var internalContext : Context? = null

    init {
        internalContext = ctx
    }

    val bluetoothManager = internalContext?.getSystemService<BluetoothManager>()
        ?: throw IllegalStateException("BluetoothManager not found")


    protected var btAdapter = BluetoothAdapter.getDefaultAdapter()
    @SuppressLint("MissingPermission")
    protected var alreadyBondedDevices = btAdapter.bondedDevices
    protected val bluetoothLeScanner = btAdapter.bluetoothLeScanner

    protected var GATT_SERVICES :  List<BluetoothGattService>? = null
    var receivingRawData : ByteArray?     = null
    var bluetoothGatt    : BluetoothGatt? = null

    //var a = mutab

    /**
     *  CALLBACKS
     */
    /////////////////////////////////
    // SCANNER CALLBACK            //
    /////////////////////////////////
    protected val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // Log.i("www","www ${result.device.name}  || [${result.device.address}]")
            //Log.i(TAG,"onScanResult: ${scanResultsX.size}")

            val indexQuery = scanResults.replayCache.last().indexOfFirst {
                it.device.address == result.device.address
            }
            /** A scan result already exists with the same address */
            if (indexQuery != -1) {
                // for closest connect
                // refreshing is about 20-60 sec rssi
                for (i in 0 until scanResults.replayCache.last().size) {
                    if (scanResults.replayCache.last()[i].device.address == result.device.address) {
                        scanResults.emit().rssi = result.rssi
                    }
                }



            } else { /** founded new device */

                //Log.i(TAGx,"Found BLE device!!! Name: ${result.device.name ?: "Unnamed"}, address: ${result.device.address}")
                if (result.device.name != null && result.device.name.toString().contains(TARGET_PART_OF_NAME,true) == true) {
                    scanResultsX.add(ScannedDevice(result.device,result.rssi))
                    scanResultsX.sortByDescending { it.rssi }
                }

            }
        }
    }
}