package com.arstagaev.liteble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.arstagaev.liteble.BleParameters.SCAN_FILTERS
import com.arstagaev.liteble.BleParameters.STATE_BLE
import com.arstagaev.liteble.BleParameters.TARGET_CHARACTERISTIC
import com.arstagaev.liteble.models.StateBle
import kotlinx.coroutines.delay
import java.util.*

class BleActions(
    ctx: Context? = null,
) : BleManager(ctx) {


    private val TAG = BleActions::class.qualifiedName
    private var scanning = false

    init {
        internalContext = ctx
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
    protected fun enableNotifications(characteristic: BluetoothGattCharacteristic) {

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
            Log.w("wcwc","wcwc isUUID-> ${(it.uuid)} <-")
            //Log.w("wcwc","wcwc isCccd-> ${(it.)} <-")

            //Log.w("wcwc","wcwc isCccd-> ${(it.())} <-")
        }

        val cccdUuid = characteristic.descriptors[0].uuid ?: UUID.fromString(BleParameters.CCC_DESCRIPTOR_UUID)
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
    fun startScan() {
        if (scanning)
            return

        Log.w(TAG,"SCAN_FILTERS: ${SCAN_FILTERS.joinToString()}   Filters isNotEmpty ${SCAN_FILTERS.isNotEmpty()}")

        if (btAdapter == null) {
            //delay(3000)
            Log.w(TAG,"BT ADAPTER IS NULL!!")
            print("BTADPRTER is NULL!!!")
            return
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
    }





    @SuppressLint("MissingPermission")
    suspend fun stopScan() {
        if (!scanning)
            return
        Log.i(TAG,"Stop scan")
        bluetoothLeScanner?.stopScan(leScanCallback)
        scanning = false
    }


}
