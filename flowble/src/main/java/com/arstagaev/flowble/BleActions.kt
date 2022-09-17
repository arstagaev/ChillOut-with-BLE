package com.arstagaev.flowble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.arstagaev.flowble.BLEStarter.Companion.outputBytesRead
import com.arstagaev.flowble.BLEStarter.Companion.scanDevices
import com.arstagaev.flowble.BleParameters.BLE_STATUS
import com.arstagaev.flowble.BleParameters.CONNECTED_DEVICE
import com.arstagaev.flowble.BleParameters.SCAN_FILTERS
import com.arstagaev.flowble.BleParameters.STATE_BLE
import com.arstagaev.flowble.BleParameters.TARGET_CHARACTERISTIC_NOTIFY
import com.arstagaev.flowble.BleParameters.scanResultsX
import com.arstagaev.flowble.gentelman_kit.hasPermission
import com.arstagaev.flowble.gentelman_kit.logAction
import com.arstagaev.flowble.gentelman_kit.logError
import com.arstagaev.flowble.gentelman_kit.logWarning
import com.arstagaev.flowble.models.StateBle
import com.arstagaev.liteble.models.ScannedDevice
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class BleActions(
    ctx: Context? = null,
) : BleManager(ctx) {

    private val TAG = this::class.qualifiedName
    private var scanning = false
    var activity: Activity? = null
    var REVERT_WORK_CAUSE_PERMISSION = false
    var scanResultsNewFoundedINTERNAL = arrayListOf<ScannedDevice>()

    init {
        internalContext = ctx
        checkPermissions()

    }

    private fun checkPermissions() {
        //check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false
                && internalContext?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false
                && internalContext?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false) {

                REVERT_WORK_CAUSE_PERMISSION = false

            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"# BLUETOOTH_SCAN:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false} #")
                Log.e(TAG,"# BLUETOOTH_CONNECT:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false} #")
                Log.e(TAG,"########################################")

                REVERT_WORK_CAUSE_PERMISSION = true
            }
        }else {
            if (internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false) {

                REVERT_WORK_CAUSE_PERMISSION = false

            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"########################################")

                REVERT_WORK_CAUSE_PERMISSION = true
            }
        }
    }

    fun checkBt(
        //activity: Activity? = null
    ) {
        if (activity != null && !btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            activity!!.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectTo(address: String) : Boolean  {
        if (address == null || address.isEmpty()){

            logError("address is null or Empty<")
            return false
        }
        if (CONNECTED_DEVICE != null && CONNECTED_DEVICE?.address == address) {
            return true
        }



        Log.d("eee","connect to ${address} <<<<<<<<<<<<<<<<<<<<")
        btAdapter.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                if (ActivityCompat.checkSelfPermission(
                        internalContext!!,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }

                if (device == null) {
                    logWarning("ble device is null!!!")
                    return false
                }
                CoroutineScope(CoroutineName("checkOfConnect")).async {

                    bluetoothGatt = device.connectGatt(internalContext, false, bluetoothGattCallback)
                    delay(1000)

                }.await()

                // check we connected or not
                var a = bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)

                logWarning("111 ${a?.joinToString() ?: "null"}  isNull:${bluetoothGatt?.services?.size ?: null}    disk${bluetoothGatt?.discoverServices()}")

                // if device don't found
                val connectedDevice = a?.find { it.address == address } ?: return false


                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.  Unable to connect.")
                return false
            }
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(uuid: UUID): Boolean {

        Log.w(TAG,"enableNotifications()()()()()()")

        bluetoothGatt?.findCharacteristic(uuid)?.let { characteristic ->
            val cccdUuid = characteristic.descriptors[0].uuid ?: UUID.fromString(CCC_DESCRIPTOR_UUID)

            val payload = when {
                characteristic.isIndicatable() ->
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() ->
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    error("${characteristic.uuid} doesn't support notifications/indications")
                    return false
                }
            }

            characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->

                if (!bluetoothGatt?.setCharacteristicNotification(characteristic, true)!!) {
                    Log.e(TAG,"setCharacteristicNotification failed for ${characteristic.uuid}")
                    //signalEndOfOperation()
                    return false
                }

                cccDescriptor.value = payload
                bluetoothGatt?.writeDescriptor(cccDescriptor)
                TARGET_CHARACTERISTIC_NOTIFY = uuid
                logAction("Success Enable Notification !! ")
                return true
            } ?: internalContext.run {
                Log.e(TAG,"${characteristic.uuid} doesn't contain the CCC descriptor!")
                //signalEndOfOperation()
            }
        } ?: internalContext.run {
            Log.e(TAG,"Cannot find $uuid! Failed to enable notifications.")
            //signalEndOfOperation()
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun disableNotifications(uuid: UUID): Boolean {
        if (uuid == null) return false
        val characteristicTarget = bluetoothGatt?.findCharacteristic(uuid = uuid)//getCharacteristic(uuid) ?: return false

        if (characteristicTarget == null) {
            Log.e("ccc","characteristic == null !!!")
            return false
        }
        Log.w(TAG,"disableNotifications()()()()()()")


        val cccdUuid = UUID.fromString(BleParameters.CCC_DESCRIPTOR_UUID)
        characteristicTarget.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!bluetoothGatt!!.setCharacteristicNotification(characteristicTarget, false)) {
                Log.e("ccc","setCharacteristicNotification failed for ${characteristicTarget.uuid}")

                return false
            }

            cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            return bluetoothGatt!!.writeDescriptor(cccDescriptor)
        } ?: internalContext.run {
            Log.e("ccc","${characteristicTarget.uuid} doesn't contain the CCC descriptor!")
            return false
        }
    }
    @SuppressLint("MissingPermission")
    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        Log.i(TAG,"writeDescriptor starts  >> ${(bluetoothManager?.getConnectedDevices(
            BluetoothProfile.GATT)?.size ?: 0)}")

        if ((bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)?.size ?: 0) > 0) {
            Log.i(TAG,"writeDescriptor already<<")
            bluetoothGatt?.let { gatt ->
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            } //?: error("Not connected to a BLE device!")

        } else {
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            STATE_BLE = StateBle.NO_CONNECTED
            //unBonding(isEmergencyReset = true)
        }

    }


    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    @SuppressLint("MissingPermission")
    fun startScan(scanFilter: ScanFilter?): Boolean {
        if (scanning)
            return true

        Log.w(TAG,"SCAN_FILTERS: ${SCAN_FILTERS.joinToString()}   Filters isEmpty: ${SCAN_FILTERS.isEmpty()}")

        if (btAdapter == null) {
            //delay(3000)
            Log.w(TAG,"BT ADAPTER IS NULL!!")
            print("BTADPRTER is NULL!!!")
            return false
        }

        Log.w(TAG,"bluetoothLeScanner>>> ${bluetoothLeScanner.toString()}")
//        if (SCAN_FILTERS.isNotEmpty()) {
//            bluetoothLeScanner.startScan(SCAN_FILTERS,scanSettings,leScanCallback)
//            print("WITH SCAN FILTERS I Scan")
//        }else {
//            bluetoothLeScanner.startScan(leScanCallback)
//            print("WITHOUT SCAN FILTERS I Scan")
//        }
        if (scanFilter!= null) {
            SCAN_FILTERS.add(scanFilter)

            bluetoothLeScanner?.startScan(
                SCAN_FILTERS,
                scanSettings,
                leScanCallback
            )
        }else {
            bluetoothLeScanner?.startScan(
                leScanCallback
            )
        }


        scanning = true
        println("Scanning is now: ${scanning}")
        return scanning
    }





    @SuppressLint("MissingPermission")
    suspend fun stopScan(): Boolean {
        if (!scanning)
            return false
        Log.i(TAG,"Stop scan")
        bluetoothLeScanner?.stopScan(leScanCallback)
        scanning = false

        return true
    }


    @SuppressLint("MissingPermission")
    fun readCharacteristic(
        uuid: UUID,
    ) : Boolean {
        if (uuid == null) return false
        val characteristicTarget = bluetoothGatt?.findCharacteristic(uuid = uuid)//getCharacteristic(uuid) ?: return false

        return if (characteristicTarget?.value != null) {
            outputBytesRead.value = characteristicTarget.value
            true
        }else {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(
        uuid: UUID,
        payload: ByteArray
    ): Boolean {
        if (uuid == null) return false
        val characteristicTarget = bluetoothGatt?.findCharacteristic(uuid = uuid)//getCharacteristic(uuid) ?: return false

        if (CONNECTED_DEVICE == null && characteristicTarget == null) {
            return false
        }

        val writeType = when {
            characteristicTarget!!.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristicTarget.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> {
                Log.e("ccc","Characteristic ${characteristicTarget?.uuid} cannot be written to")
                return false
            }
        }

        characteristicTarget.let { characteristic ->
            characteristic.writeType = writeType
            characteristic.value = payload
            bluetoothGatt!!.writeCharacteristic(characteristic)
        } ?: internalContext.run {
            Log.e("","Cannot find  to write to")
        }
        return true
    }

//    fun getCharacteristic(uuid: UUID): BluetoothGattCharacteristic?{
//        GATT_SERVICES?.forEachIndexed { index, bluetoothGattService ->
//            var a = bluetoothGattService.getCharacteristic(uuid)
//            if (a != null && a.uuid == uuid) {
//                println(" I FOUND CHARACTERISTIC: ${a.uuid.toString()}")
//                return a
//            }
//        }
////        if (GATT_SERVICES != null) {
////            for (i  in 0 until GATT_SERVICES.size) {
////                for (z in 0 until BleParameters.GATT_SERVICES[i].characteristics.size) {
////                    if (BleParameters.GATT_SERVICES[i].characteristics[z].uuid == uuid) {
////                        println(" I FOUND CHARACTERISTIC: ${BleParameters.GATT_SERVICES[i].characteristics[z].uuid.toString()}")
////                        return BleParameters.GATT_SERVICES[i].characteristics[z]
////                    }
////                }
////            }
////        }
//
//        println(" I DONT FOUND CHARACTERISTIC:")
//        return null
//    }

    //////////////////////////////////////////////////////////
    // Callbacks                                            //
    //////////////////////////////////////////////////////////
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println("scan~ error >> ${errorCode}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            BleParameters.scanResultsX.value =results as ArrayList<ScannedDevice>

            //scanResultAlternative = results!!
            println("scan~ batch >> ${results?.joinToString()}")
        }
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            //println("<>>>> ${result.device.address}")

            //Log.i("FOUNDED","device: ${result.device.address ?: "nuller"}  ${scanResults.value?.size} rssi: ${result.rssi}")
//            if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST) { // not working
//            }

            //println("%%%%%%%%%%%%%% ${result.device.name} ${result.device.address} rssi:${result.rssi} ${callbackType}")

            //Log.d("TAG","scan~ Pizdec  ${result.device.name} ${result.device.address} rssi:${result.rssi} ${callbackType}")

//            when(callbackType) {
//                ScanSettings.CALLBACK_TYPE_MATCH_LOST -> {
//                    println("CALLBACK_TYPE_MATCH_LOST ")
//                }
//                ScanSettings.CALLBACK_TYPE_FIRST_MATCH -> {
//                    println("CALLBACK_TYPE_FIRST_MATCH ")
//                }
//                ScanSettings.CALLBACK_TYPE_ALL_MATCHES -> {
//                    println("CALLBACK_TYPE_ALL_MATCHES ")
//                }
//                //ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT -> {}
//            }
            val indexQuery = scanResultsX.value.indexOfFirst {
                it.bt.address == result.device.address
            }
            /** A scan result already exists with the same address */
            if (indexQuery != -1) {
                // for closest connect
                // refreshing is about 20-60 sec rssi
                if (scanResultsX.value != null && scanResultsX.value!!.isNotEmpty()) {

                    for (i in 0 until scanResultsX.value!!.size) {
                        if (scanResultsX.value!![i].bt.address == result.device.address) {
                            scanResultsX.value!![i].let {
                                //it.scanResult = result
                            }
                            scanResultsX.value!![i].rssi = result.rssi
                            //scanResults.value!![i].lastRefresh = System.currentTimeMillis() / 1000L
                        }
                    }

                }
                scanResultsX.value?.sortByDescending { it.rssi }
                GlobalScope.launch {
                    scanDevices.emit(scanResultsX.value)
                }

//                GlobalScope.launch {
//                    scanResultsX.emit(scanResultsNewFoundedINTERNAL)
//                }
                //println(" >>> ${BleParameters.scanResultsX.value.joinToString()}")
            } else { /** founded new device */
                scanResultsNewFoundedINTERNAL.add(
                    ScannedDevice(
                        result.device,
                        result.rssi
                    )
                )
                scanResultsX.value =scanResultsNewFoundedINTERNAL
                //scanResultsNewFoundedINTERNAL.sortByDescending { it.rssi }
                GlobalScope.launch {
                    scanDevices.emit(scanResultsNewFoundedINTERNAL)
                }


                println(" >>>>>> ${BleParameters.scanResultsX.value.joinToString()}")
            }
        }
    }



    @SuppressLint("MissingPermission")
    fun disconnectFromDevice(): Boolean {
        Log.w(TAG,"disconnectFromDevice()()()()()()()")
//        if (ActivityCompat.checkSelfPermission(
//                internalContext!!,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
        //toastShow("Disconnecting ..",internalContext!!)
        if (bluetoothGatt != null){
            bluetoothGatt?.close()
            bluetoothGatt?.disconnect()
            bluetoothGatt = null
            Log.w(TAG," disconnected FromDevice !!! ")
        }else {
            Log.w(TAG," bluetoothGatt is NULL ")
        }

        return false
    }
    suspend fun disableBLEManager(): Boolean {
        //updateNotificationFlow.cancellable()


//        if (TARGET_CHARACTERISTIC_NOTIFY != null) {
//            disableNotifications(TARGET_CHARACTERISTIC_NOTIFY!!)
//        }
        stopScan()
        delay(10L)
        disconnectFromDevice()
        delay(30)
//        if (gattUpdateReceiver != null) {
//            internalContext?.unregisterReceiver(gattUpdateReceiver)
//        }
        Log.w(TAG," disableBLEManager !!! ")
        Log.w(TAG," disableBLEManager !!! ")
        Log.w(TAG," disableBLEManager !!! ")
        return true
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

}
