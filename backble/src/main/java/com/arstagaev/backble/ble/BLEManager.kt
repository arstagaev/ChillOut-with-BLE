package com.arstagaev.backble.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.getSystemService
import com.arstagaev.backble.ble.BLEParameters.ACTION_GATT_CONNECTED
import com.arstagaev.backble.ble.BLEParameters.ACTION_GATT_DISCONNECTED
import com.arstagaev.backble.ble.BLEParameters.ACTION_GATT_SERVICES_DISCOVERED
import com.arstagaev.backble.ble.BLEParameters.BLE_STATUS
import com.arstagaev.backble.ble.BLEParameters.CONNECTED_DEVICE
import com.arstagaev.backble.ble.BLEParameters.STATE_NOW_SERVICE
import com.arstagaev.backble.ble.BLEParameters.MSG
import com.arstagaev.backble.ble.BLEParameters.TARGET_PART_OF_NAME
import com.arstagaev.backble.ble.BLEParameters.TRACKED_BLE_DEVICE
import com.arstagaev.backble.ble.BLEParameters.TRACKED_NAME_OF_BLE_DEVICE
import com.arstagaev.backble.ble.BLEParameters.scanResultsX
import com.arstagaev.backble.ble.enums.StateOfService
import com.arstagaev.backble.ble.models.ScannedDevice
import com.arstagaev.backble.core.CoreParameters.TIME_OF_TRIP
import com.arstagaev.backble.gentelman_kit.bytesToHex
import com.arstagaev.chilloutble.utils.toastShow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

abstract class BLEManager(ctx : Context)  {

    protected val TAG = BLEManager::class.qualifiedName
    internal var internalContext : Context? = null
    internal val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_GATT_CONNECTED -> {
                    connected = true
                    //updateConnectionState(R.string.connected)
                }
                ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    //updateConnectionState(R.string.disconnected)
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    //displayGattServices(getSupportedGattServices() as List<BluetoothGattService>?)
                }
            }
        }
    }
    init {
        internalContext = ctx
        internalContext!!.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    //var ble_manager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);


    protected var btAdapter = BluetoothAdapter.getDefaultAdapter()
    @SuppressLint("MissingPermission")
    protected var alreadyBondedDevices = btAdapter.bondedDevices
    protected val bluetoothLeScanner = btAdapter.bluetoothLeScanner
    var scanning = false
    var advScanning = false
    //protected val handler = Handler()
    protected var GATT_SERVICES :  List<BluetoothGattService>? = null
    var receivingRawData : ByteArray? = null

    var bluetoothGatt: BluetoothGatt? = null
    var connected = false
    var mGattCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

    val bluetoothManager = internalContext?.getSystemService<BluetoothManager>()
        ?: throw IllegalStateException("BluetoothManager not found")

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

            val indexQuery = scanResultsX.indexOfFirst {
                it.bt.address == result.device.address }
            /** A scan result already exists with the same address */
            if (indexQuery != -1) {
                // for closest connect
                // refreshing is about 20-60 sec rssi
                for (i in 0 until scanResultsX.size) {
                    if (scanResultsX[i].bt.address == result.device.address) {
                        scanResultsX[i].rssi = result.rssi
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
    /////////////////////////////////
    // ADV SCANNER CALLBACK        //
    /////////////////////////////////
    protected val advertisingCallback: ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // Log.i("www","www ${result.device.name}  || [${result.device.address}]")
            Log.i(TAG,"ADVERTISE onScanResult: ${scanResultsX.size}")
            /** for advertising */
            if (result.device.name != null && result.device.name.toString().contains(
                    TRACKED_NAME_OF_BLE_DEVICE ?: "itelma",true)) {

                TRACKED_BLE_DEVICE = ScannedDevice(result.device,result.rssi)

            }

        }
    }



    val REP = "GATT"
    protected val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            BLEParameters.BLE_STATUS = status
            Log.w(REP,"gatt status:${BLE_STATUS} \n${gatt?.printGattTable()} <<|")
            toastShow("status=${BLE_STATUS}",internalContext!!)

            when(status) {
                //0 ->  { NEED_RESET = false }
                8  -> {
                    toastShow("8!!!8888888!!!",internalContext!!)
                    STATE_NOW_SERVICE = StateOfService.LOSS_CONNECTION_AND_WAIT_NEW
                }
                19 -> {
                    //NEED_RESET = true
                }
            }
            when(newState) {
                BluetoothProfile.STATE_DISCONNECTED  -> { Log.w("www", "state:  $newState  STATE_DISCONNECTED  ")
                    STATE_NOW_SERVICE = StateOfService.NO_CONNECTED
                    MSG = "STATE_DISCONNECTED"
                    //generateNameOfAllLogPerSession()

                }
                BluetoothProfile.STATE_CONNECTING    -> { Log.w("www", "state:  $newState  STATE_CONNECTING    ")
                    STATE_NOW_SERVICE = StateOfService.CONNECTING
                }
                BluetoothProfile.STATE_CONNECTED     -> {
                    Log.w("www", "state:  $newState  STATE_CONNECTED     ")
                    //generateNameOfAllLogPerSession()
                    if (gatt != null) {
                        CONNECTED_DEVICE = gatt?.device
                    }

                    MSG = "STATE_CONNECTED"
                    STATE_NOW_SERVICE = StateOfService.CONNECTED_BUT_NO_SUBSCRIBED

                    // Attempts to discover services after successful connection.
                    bluetoothGatt?.discoverServices()
                    //displayGattServices(bluetoothGatt!!.services)
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.w("www", "state:  $newState  STATE_DISCONNECTING ")
                    STATE_NOW_SERVICE = StateOfService.DISCONNECTING
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)

                Log.w("www", "onServicesDiscovered received:  $status  GATT_SUCCESS")
                //displayGattServices(gatt?.services)
                GATT_SERVICES = getSupportedGattServices() as List<BluetoothGattService>?

            } else {
                Log.w("www", "onServicesDiscovered received: $status")
            }
        }
        val firstByteStart : Byte = 0x01
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {

            MSG = bytesToHex(characteristic.value)
            //Log.w("www", "state:  $CURRENT_SERVICE_STATE  NOTIFYING_OR_INDICATING ")
            Log.w("www","www >>> ${bytesToHex(characteristic.value)}<<")

            if (receivingRawData?.get(0) == firstByteStart) {

                Log.w(TAG,"FIRST BYTE !!$")
                //generateNameOfAllLogPerSession()
                TIME_OF_TRIP = 0
                //NEED_RESET = false
            }
    //            asd = flow {
    //                emit()
    //            }
            receivingRawData = characteristic.value
            //WritingToFile().router(receivingRawData!!)

            if (STATE_NOW_SERVICE == StateOfService.CONNECTED_BUT_NO_SUBSCRIBED && characteristic.value.size > 240){
            }
            STATE_NOW_SERVICE = StateOfService.NOTIFYING_OR_INDICATING
        }
    }
    var asd : Flow<Int> = emptyFlow()

    val job = CoroutineScope(Dispatchers.Main).launch {

        val a = flow  {

            while (true) {
                delay(100)
                emit("")
            }
        }
        a.takeWhile { it != "someString" }

        val flow = flow {
            for (i in 1..10){
                delay(1000)
                emit(i)
            }
        }

    }

    var updateNotificationFlow : Flow<ByteArray> = flow {
        takeIf { STATE_NOW_SERVICE == StateOfService.NOTIFYING_OR_INDICATING }
        while(true) {

            if (receivingRawData == null) {
                emit(byteArrayOf(0x00)) // Emits the result of the request to the flow
                delay(1000) // Suspends the coroutine for some time
            }else {
                emit(receivingRawData!!) // Emits the result of the request to the flow
                delay(1000) // Suspends the coroutine for some time
            }

        }
    }

    /**
     * UTILITY INTERNAL METHODS
     */
    fun getSupportedGattServices(): List<BluetoothGattService?>? {

        return bluetoothGatt?.services
    }


    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(ACTION_GATT_CONNECTED)
            addAction(ACTION_GATT_DISCONNECTED)
        }
    }



}