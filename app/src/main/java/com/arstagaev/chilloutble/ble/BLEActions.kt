package com.arstagaev.chilloutble.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.arstagaev.backble.ble.*
import com.arstagaev.chilloutble.ble.BLEParameters.STATE_NOW_SERVICE
import com.arstagaev.chilloutble.ble.BLEParameters.SCAN_FILTERS
import com.arstagaev.chilloutble.ble.BLEParameters.TARGET_CHARACTERISTIC
import com.arstagaev.chilloutble.ble.BLEParameters.WITH_NOTIFY
import com.arstagaev.chilloutble.ble.BLEParameters.isADVERTISING_WORK
import com.arstagaev.backble.ble.enums.StateOfService
import com.arstagaev.chilloutble.ble.control_module.enum.ActionOfService
import com.arstagaev.chilloutble.gentelman_kit.hexToBytes
import com.arstagaev.chilloutble.utils.toastShow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import java.util.*
import kotlin.concurrent.fixedRateTimer

class BLEActions(ctx : Context) : BLEManager(ctx) {

    init {

    }

    suspend fun connectTo(address: String) : Boolean  {
        if (address == null){
            Log.e("eee","eee address is null <")
            return false
        }
        toastShow("Connecting ..",internalContext!!)
        Log.d("eee","connect to ${address} <<<<<<<<<<<<<<<<<<<<")
        btAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                if (ActivityCompat.checkSelfPermission(
                        internalContext!!,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return false
                }
                //public static final int DEVICE_TYPE_CLASSIC = 1;
                //    public static final int DEVICE_TYPE_DUAL = 3;
                //    public static final int DEVICE_TYPE_LE = 2;
                //    public static final int DEVICE_TYPE_UNKNOWN = 0; tag my: DEVICE_TYPE_LE
                Log.i(TAG,">> >> Type of device: ${device.type}")
                bluetoothGatt = device.connectGatt(internalContext, false, bluetoothGattCallback)
                //return@coroutineScope true
                //SUPER_BLE_DEVICE = bluetoothGatt?.device
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.  Unable to connect.")
                //return@coroutineScope false
                return false
            }
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
            //return@coroutineScope false
        }
    }

    @SuppressLint("MissingPermission")
    protected fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!WITH_NOTIFY || BLEParameters.ACTION_NOW_SERVICE == ActionOfService.STOP) {
            Log.w(TAG,"WITHOUT NOTIFICATION")
            Log.w(TAG,"WITHOUT NOTIFICATION")
            Log.w(TAG,"WITHOUT NOTIFICATION")
            return
        }
        if(BLEParameters.ACTION_NOW_SERVICE == ActionOfService.STOP) {
            Log.w(TAG,"coz ActionOfService.STOP")
            Log.w(TAG,"coz ActionOfService.STOP")
            Log.w(TAG,"coz ActionOfService.STOP")
            return
        }
//        if (bluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
//            Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
//            return
//        }else {
//            TARGET_CHARACTERISTIC = characteristic
//        }
        Log.w(TAG,"enableNotifications()()()()()()")
        Log.w("wcwc","wcwc characteristic.uuid -> ${characteristic.uuid.toString()} <-")
        Log.w("wcwc","wcwc characteristic.uuid -> ${characteristic.descriptors.size} <-")
        characteristic.descriptors.forEach {
            Log.w("wcwc","wcwc isCccd-> ${(it.isCccd())} <-")
            Log.w("wcwc","wcwc printProperties-> ${(it.printProperties())} <-")
            Log.w("wcwc","wcwc isUUID-> ${(it.uuid)} <-")
            //Log.w("wcwc","wcwc isCccd-> ${(it.)} <-")

            //Log.w("wcwc","wcwc isCccd-> ${(it.())} <-")
        }

        val cccdUuid = characteristic.descriptors[0].uuid ?: UUID.fromString(BLEParameters.CCC_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
                Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }else {
                TARGET_CHARACTERISTIC = characteristic
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")

    }

    @SuppressLint("MissingPermission")
    protected fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (characteristic == null) {
            Log.e("ccc","characteristic == null !!!")
            return
        }
        Log.w(TAG,"disableNotifications()()()()()()")


        val cccdUuid = UUID.fromString(BLEParameters.CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!bluetoothGatt!!.setCharacteristicNotification(characteristic, false)) {
                Log.e("ccc","setCharacteristicNotification failed for ${characteristic.uuid}")

                return
            }

            cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            bluetoothGatt!!.writeDescriptor(cccDescriptor)
        } ?: internalContext.run {
            Log.e("ccc","${characteristic.uuid} doesn't contain the CCC descriptor!")

        }
    }


    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
//    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
//        if (gattServices == null) return
//        var uuid: String?
//        //val unknownServiceString: String = resources.getString(R.string.unknown_service)
//        //val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
//        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
//        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
//            mutableListOf()
//        Log.w("www","www gattServices ${gattServices.joinToString()}")
//
//        //
//        if (gattServices.isEmpty()) {
//            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first  ???????")
//            return
//        }
//        gattServices.forEach { service ->
//            val characteristicsTable = service.characteristics.joinToString(
//                separator = "\n|--",
//                prefix = "|--"
//            ) { it.uuid.toString()
//
//            }
//
//            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
//            service.characteristics.forEach {
//                if (WITH_NOTIFY && it.uuid.toString() == BLEParameters.CHARACTERISTIC_UUID_1) {
//                    enableNotifications(it)
//                }
//            }
//
//        }
//
//        // Loops through available GATT Services.
//        gattServices.forEach { gattService ->
//            val currentServiceData = HashMap<String, String>()
//            uuid = gattService.uuid.toString()
////            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
////            currentServiceData[LIST_UUID] = uuid
////            gattServiceData += currentServiceData
//
//            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
//            val gattCharacteristics = gattService.characteristics
//            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()
//
//            // Loops through available Characteristics.
//            gattCharacteristics.forEach { gattCharacteristic ->
//                charas += gattCharacteristic
//                val currentCharaData: HashMap<String, String> = hashMapOf()
//                uuid = gattCharacteristic.uuid.toString()
//                Log.w("www","www gattCharacteristic ${gattCharacteristic.toString()}")
////                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
////                currentCharaData[LIST_UUID] = uuid!!
////                gattCharacteristicGroupData += currentCharaData
//
//
//            }
//            mGattCharacteristics += charas
//            gattCharacteristicData += gattCharacteristicGroupData
//        }
//    }
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    @SuppressLint("MissingPermission")
    suspend fun startScan() {
        if (scanning)
            return

        Log.w(TAG,"SCAN_FILTERS: ${SCAN_FILTERS.joinToString()}")

        bluetoothLeScanner.startScan(SCAN_FILTERS,scanSettings,leScanCallback)
        scanning = true

        delay( if (isADVERTISING_WORK) BLEParameters.SCAN_PERIOD_ADV else BLEParameters.SCAN_PERIOD)

        if (scanning) {
            bluetoothLeScanner.stopScan(leScanCallback)

        }
        scanning = false

//        if (isADVERTISING_WORK) {
//            startScan()
//        }
    }

    //suspend fun startScanInfinity() {}



    @SuppressLint("MissingPermission")
    suspend fun stopScan() {
        if (!scanning)
            return

        bluetoothLeScanner.stopScan(leScanCallback)
        scanning = true
    }

    suspend fun startAdvertisingScan() {
        if (advScanning)
            return

        if (ActivityCompat.checkSelfPermission(
                internalContext!!,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        bluetoothLeScanner.startScan(advertisingCallback)
        Log.i(TAG,"ADVERTISE STARTED !!!!!!!!!!!!!!!")
        advScanning = true
        Log.i("Adviser Ins"," isScanningAdvertise: ${advScanning}   isScanningDefault: ${scanning ?: "null"} ")

        delay(BLEParameters.SCAN_PERIOD_ADV)

        if (advScanning) {
            bluetoothLeScanner.stopScan(advertisingCallback)

        }
        advScanning = false
        Log.i(TAG,"ADVERTISE STOPPED !!!!!!!!!!!!!!!")
    }

    @SuppressLint("MissingPermission")
    suspend fun stopAdvertisingScan() {
        if (!advScanning)
            return

        bluetoothLeScanner.stopScan(advertisingCallback)
        advScanning = true
    }

    @SuppressLint("MissingPermission")
    fun findTargetDeviceFromBond() {
        var index = 0
        fixedRateTimer("",daemon = false,0,5000) {
            if (index > alreadyBondedDevices.size-1) {
                this.cancel()
            } else {
                if (alreadyBondedDevices.elementAt(index).name.contains("itelma",ignoreCase = true)) {
                    Log.w("www","www ~~~~~~~> ${alreadyBondedDevices.elementAt(index).name}")
                }
            }
            index++
        }
    }

    fun showMeCharacteristics() {
        if (GATT_SERVICES == null || GATT_SERVICES!!.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first ????")
            Toast.makeText(internalContext,"Need discover services",Toast.LENGTH_LONG).show()
            return
        }
        GATT_SERVICES!!.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) {
                it.uuid.toString()
            }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
        }
    }


    suspend fun manageNotifyIndicate(isChecked: Boolean) {
        if (GATT_SERVICES == null || GATT_SERVICES!!.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first ????")
            Toast.makeText(internalContext,"Need discover services",Toast.LENGTH_LONG).show()
            return
        }
        Log.w(TAG,"trying turn $isChecked indicate")
        //
        Log.w(TAG,"NOTIFY IS INITIALIZING LIKE ${WITH_NOTIFY}")
        Log.w(TAG,"NOTIFY IS INITIALIZING LIKE ${WITH_NOTIFY}")
        Log.w(TAG,"NOTIFY IS INITIALIZING LIKE ${WITH_NOTIFY}")

        GATT_SERVICES!!.forEach { service ->
            service.characteristics.forEach {

                if (WITH_NOTIFY && it.uuid.toString() == BLEParameters.CHARACTERISTIC_UUID_1) {
                    if (isChecked) {
                        enableNotifications(it)
                    } else {
                        disableNotifications(it)
                    }
                    delay(100)
                }

            }
        }
    }


    @SuppressLint("MissingPermission")
    fun writeCharacteristic(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {

        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> {
                Log.e("ccc","Characteristic ${characteristic.uuid} cannot be written to")
                return
            }
        }
        //CharacteristicWrite(device, characteristic.uuid, writeType, payload)
        characteristic?.let { characteristic ->
            characteristic.writeType = writeType
            characteristic.value = payload
            bluetoothGatt!!.writeCharacteristic(characteristic)
        } ?: internalContext.run {
            Log.e("","Cannot find  to write to")
        }
//        if (device.isConnected()) {
//
//        } else {
//            Timber.e("Not connected to ${device.address}, cannot perform characteristic write")
//        }
    }

    @SuppressLint("MissingPermission")
    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        Log.i(TAG,"writeDescriptor starts  >> ${(bluetoothManager.getConnectedDevices(
            BluetoothProfile.GATT)?.size ?: 0)}")

        if ((bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)?.size ?: 0) > 0) {
            Log.i(TAG,"writeDescriptor already<<")
            bluetoothGatt?.let { gatt ->
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            } //?: error("Not connected to a BLE device!")

        } else {
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            Log.e(TAG,"// // Cant enable|disable notification !! ")
            STATE_NOW_SERVICE = StateOfService.NO_CONNECTED
            //unBonding(isEmergencyReset = true)
        }

    }

    suspend fun unBonding(isEmergencyReset : Boolean) {
        //Toast.makeText(internalContext,"unBonding ... ",Toast.LENGTH_LONG).show()
        if (GATT_SERVICES == null || GATT_SERVICES!!.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first ????")
            Toast.makeText(internalContext,"Need discover services",Toast.LENGTH_LONG).show()
            return
        }
        Log.w(TAG,"UNBONDING starts ... ${STATE_NOW_SERVICE.name}>>")
        toastShow("UnBonding ..",internalContext!!)
        //
        if (bluetoothGatt == null) {
            Log.w(TAG,"UNBONDING starts but bluetooth gatt == null")
            STATE_NOW_SERVICE = StateOfService.NO_CONNECTED
        }

        if (isEmergencyReset) {
            //manageNotifyIndicate(false)

            GATT_SERVICES!!.forEach { service ->
                service.characteristics.forEach {

                    if (it.uuid.toString() == BLEParameters.CHARACTERISTIC_UUID_2){
                        writeCharacteristic(bluetoothGatt!!.device,it, hexToBytes(BLEParameters.SEND_TO_UNBOND)) // been 01

                    }
                }
            }

            delay(50)
            bluetoothGatt?.device?.removeBond()
            delay(100)
            disconnectFromDevice()
        }else {
            try {
                manageNotifyIndicate(false)
            }catch (e: Exception) {
                Log.e("ERROR","ERROR CONNECTED_BUT_NO_SUBSCRIBED: ${e.message}")
            }


            delay(100)
            GATT_SERVICES!!.forEach { service ->
                service.characteristics.forEach {

                    if (it.uuid.toString() == BLEParameters.CHARACTERISTIC_UUID_2){
                        writeCharacteristic(bluetoothGatt!!.device,it, hexToBytes(BLEParameters.SEND_TO_UNBOND)) // been 01

                    }
                }
            }

            delay(500)
            bluetoothGatt?.device?.removeBond()
            delay(1000)
            disconnectFromDevice()
        }
        Log.w(TAG,"UNBONDING finish << ${STATE_NOW_SERVICE.name}")
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice() {
        Log.w(TAG,"disconnectFromDevice()()()()()()()")
//        if (ActivityCompat.checkSelfPermission(
//                internalContext!!,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
        toastShow("Disconnecting ..",internalContext!!)
        if (bluetoothGatt != null){
            bluetoothGatt?.close()
            bluetoothGatt?.disconnect()
            bluetoothGatt = null
            Log.w(TAG," disconnected FromDevice !!! ")
        }else {
            Log.w(TAG," bluetoothGatt is NULL ")
        }

    }

    suspend fun disableBLEManager() {
        updateNotificationFlow.cancellable()


        if (TARGET_CHARACTERISTIC != null) {
            disableNotifications(TARGET_CHARACTERISTIC!!)
        }
        stopScan()
        delay(10L)
        disconnectFromDevice()
        delay(30)
        if (gattUpdateReceiver != null) {
            internalContext?.unregisterReceiver(gattUpdateReceiver)
        }
        Log.w(TAG," disableBLEManager !!! ")
        Log.w(TAG," disableBLEManager !!! ")
        Log.w(TAG," disableBLEManager !!! ")
    }


    fun caseFirst() {

        var timer = object : CountDownTimer(10000,1000){
            override fun onTick(p0: Long) {
            }
            override fun onFinish() {

            }
        }.start()
    }


}