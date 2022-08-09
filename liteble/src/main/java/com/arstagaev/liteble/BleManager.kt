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