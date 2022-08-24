package com.arstagaev.liteble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.arstagaev.liteble.BleParameters.ACTION_GATT_CONNECTED
import com.arstagaev.liteble.BleParameters.ACTION_GATT_DISCONNECTED
import com.arstagaev.liteble.BleParameters.BLE_STATUS
import com.arstagaev.liteble.BleParameters.CONNECTED_DEVICE
import com.arstagaev.liteble.BleParameters.STATE_BLE
import com.arstagaev.liteble.BleParameters.scanResults
import com.arstagaev.liteble.gentelman_kit.bytesToHex
import com.arstagaev.liteble.models.ScannedDevice
import com.arstagaev.liteble.models.StateBle

open class BleManager(
    ctx : Context? = null,
//    override var bluetoothGattCallback: BluetoothGattCallback,
//    override var bluetoothLeScanner: BluetoothLeScanner,
//    override var leScanCallback: ScanCallback
)  {

    private val TAG = BleManager::class.qualifiedName
    internal var internalContext : Context? = null

    init {
        internalContext = ctx
    }

    val bluetoothManager = internalContext?.getSystemService<BluetoothManager>()
        //?: throw IllegalStateException("BluetoothManager not found")


    // From the previous section:
    val btAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = internalContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    @SuppressLint("MissingPermission")
    protected var alreadyBondedDevices = btAdapter.bondedDevices
    var bluetoothLeScanner =   btAdapter.bluetoothLeScanner



    var receivingRawData : ByteArray?     = null
    var bluetoothGatt    : BluetoothGatt? = null


    /**
     *  CALLBACKS
     */
    var bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            BleParameters.BLE_STATUS = status
            Log.w("bleble","gatt status:${BleParameters.BLE_STATUS} \n${gatt?.printGattTable()} <<|")
            //toastShow("status=${BLE_STATUS}",internalContext!!)
            arrayListOf<BleOperations>(BleOperations.DELAY)

            var a = BleConductor(
                "FINGER",
                "ROLLS"
            )
            a.tapeInstructions
            //TapeRecorder
            var asd = BleOperations.CONNECT.also { it.macAddress = "00:00:00:00:00:00:00:00" }
            BleParameters.operationQueue.add(BleOperations.CONNECT.also { it.macAddress = "" })
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
                BleParameters.GATT_SERVICES = gatt?.services as List<BluetoothGattService>?

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