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



var bluetoothGattCallback = object : BluetoothGattCallback() {
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        BleParameters.BLE_STATUS = status
        Log.w("bleble","gatt status:${BleParameters.BLE_STATUS} \n${gatt?.printGattTable()} <<|")
        //toastShow("status=${BLE_STATUS}",internalContext!!)
        arrayListOf<BleOperations>(BleOperations.DELAY)
        var asd = BleOperations.CONNECT.also { it.macAddress = "" }
        operationQueue.add(BleOperations.CONNECT.also { it.macAddress = "" })
        when(status) {
            //0 ->  { NEED_RESET = false }
            8  -> {
                //toastShow("8!!!8888888!!!",internalContext!!)
                //STATE_NOW_SERVICE = StateOfService.LOSS_CONNECTION_AND_WAIT_NEW
            }
            19 -> {
                //NEED_RESET = true
            }
        }
        when(newState) {
            BluetoothProfile.STATE_DISCONNECTED  -> { Log.w("www", "state:  $newState  STATE_DISCONNECTED  ")
                BleParameters.STATE_BLE = StateBle.NO_CONNECTED
                //MSG = "STATE_DISCONNECTED"
                //generateNameOfAllLogPerSession()

            }
            BluetoothProfile.STATE_CONNECTING    -> { Log.w("www", "state:  $newState  STATE_CONNECTING    ")
                BleParameters.STATE_BLE = StateBle.CONNECTING
            }
            BluetoothProfile.STATE_CONNECTED     -> {
                Log.w("www", "state:  $newState  STATE_CONNECTED     ")
                //generateNameOfAllLogPerSession()
                if (gatt != null) {
                    BleParameters.CONNECTED_DEVICE = gatt?.device
                }

                BleParameters.STATE_BLE = StateBle.CONNECTED_BUT_NO_SUBSCRIBED

                // Attempts to discover services after successful connection.
                //bluetoothGatt?.discoverServices()
                //displayGattServices(bluetoothGatt!!.services)
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                Log.w("www", "state:  $newState  STATE_DISCONNECTING ")
                BleParameters.STATE_BLE = StateBle.DISCONNECTING
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)

            Log.w("www", "onServicesDiscovered received:  $status  GATT_SUCCESS")
            //displayGattServices(gatt?.services)
            GATT_SERVICES = gatt?.services as List<BluetoothGattService>?

        } else {
            Log.w("www", "onServicesDiscovered received: $status")
        }
    }
    val firstByteStart : Byte = 0x01
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {

        //MSG = bytesToHex(characteristic.value)
        //Log.w("www", "state:  $CURRENT_SERVICE_STATE  NOTIFYING_OR_INDICATING ")
        Log.w("www","www >>> ${bytesToHex(characteristic.value)}<<")

//        if (receivingRawData?.get(0) == firstByteStart) {
//
//            //Log.w(TAG,"FIRST BYTE !!$")
//
//        }

        //receivingRawData = characteristic.value

        if (BleParameters.STATE_BLE == StateBle.CONNECTED_BUT_NO_SUBSCRIBED && characteristic.value.size > 240){
        }
        BleParameters.STATE_BLE = StateBle.NOTIFYING_OR_INDICATING
    }
}

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