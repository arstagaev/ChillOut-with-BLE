package com.arstagaev.flowble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.getSystemService
import com.arstagaev.flowble.BLEStarter.Companion.outputBytesNotifyIndicate
import com.arstagaev.flowble.BLEStarter.Companion.outputBytesRead
import com.arstagaev.flowble.BleParameters.ACTION_GATT_CONNECTED
import com.arstagaev.flowble.BleParameters.ACTION_GATT_DISCONNECTED
import com.arstagaev.flowble.extensions.printGattTable
import com.arstagaev.flowble.models.CharacterCarrier
import com.arstagaev.flowble.models.StateBle
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BleManager(
    ctx : Context? = null
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
    var alreadyBondedDevices = btAdapter.bondedDevices
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
            Log.w("ble","gatt status:${BleParameters.BLE_STATUS} \n${gatt?.printGattTable()} <<|")

            when(newState) {
                BluetoothProfile.STATE_DISCONNECTED  -> { Log.w("www", "state:  $newState  STATE_DISCONNECTED  ")
                    BleParameters.STATE_BLE = StateBle.NO_CONNECTED
                    BleParameters.CONNECTED_DEVICE = null
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

                Log.w("www", "onServicesDiscovered received:  $status  GATT_SUCCESS")

                BleParameters.GATT_SERVICES = gatt?.services as List<BluetoothGattService>?

            } else {
                Log.w("www", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            CoroutineScope(CoroutineName("onCharacteristicChanged")).launch {

                outputBytesNotifyIndicate.emit(
                    CharacterCarrier(
                        uuidCharacteristic = characteristic?.uuid,
                        value = characteristic?.value
                    )
                )

                println("charact: ${characteristic.uuid}")
            }

            BleParameters.STATE_BLE = StateBle.NOTIFYING_OR_INDICATING
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            CoroutineScope(CoroutineName("onCharacteristicRead")).launch {

                outputBytesRead.emit(
                    CharacterCarrier(
                        uuidCharacteristic = characteristic?.uuid,
                        value = characteristic?.value,
                        codeStatus = status
                    )
                )

            }

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

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