package com.arstagaev.flowble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.arstagaev.flowble.BLEStarter.Companion.outputBytesRead
import com.arstagaev.flowble.BleParameters.CONNECTED_DEVICE
import com.arstagaev.flowble.BleParameters.GATT_SERVICES
import com.arstagaev.flowble.BleParameters.SCAN_FILTERS
import com.arstagaev.flowble.BleParameters.STATE_BLE
import com.arstagaev.flowble.BleParameters.TARGET_CHARACTERISTIC
import com.arstagaev.flowble.models.StateBle
import kotlinx.coroutines.delay
import java.util.*

class BleActions(
    ctx: Context? = null,
) : BleManager(ctx) {

    private val TAG = BleActions::class.qualifiedName
    private var scanning = false
    var activity: Activity? = null


    init {
        internalContext = ctx

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
        if (address == null){
            Log.e("eee","eee address is null <")
            return false
        }
        //toastShow("Connecting ..",internalContext!!)
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
    fun enableNotifications(uuid: UUID): Boolean {

        Log.w(TAG,"enableNotifications()()()()()()")

        bluetoothGatt?.findCharacteristic(uuid)?.let { characteristic ->
            val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
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
    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (characteristic == null) {
            Log.e("ccc","characteristic == null !!!")
            return
        }
        Log.w(TAG,"disableNotifications()()()()()()")


        val cccdUuid = UUID.fromString(BleParameters.CCC_DESCRIPTOR_UUID)
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
    fun startScan(): Boolean {
        if (scanning)
            return true

        Log.w(TAG,"SCAN_FILTERS: ${SCAN_FILTERS.joinToString()}   Filters isNotEmpty ${SCAN_FILTERS.isNotEmpty()}")

        if (btAdapter == null) {
            //delay(3000)
            Log.w(TAG,"BT ADAPTER IS NULL!!")
            print("BTADPRTER is NULL!!!")
            return false
        }

        Log.w(TAG," ${bluetoothLeScanner.toString()}")
//        if (SCAN_FILTERS.isNotEmpty()) {
//            bluetoothLeScanner.startScan(SCAN_FILTERS,scanSettings,leScanCallback)
//            print("WITH SCAN FILTERS I Scan")
//        }else {
//            bluetoothLeScanner.startScan(leScanCallback)
//            print("WITHOUT SCAN FILTERS I Scan")
//        }
        bluetoothLeScanner?.startScan(null,scanSettings,leScanCallback)
        scanning = true
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
    suspend fun disableBLEManager() {
        //updateNotificationFlow.cancellable()


        if (TARGET_CHARACTERISTIC != null) {
            disableNotifications(TARGET_CHARACTERISTIC!!)
        }
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
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

}
