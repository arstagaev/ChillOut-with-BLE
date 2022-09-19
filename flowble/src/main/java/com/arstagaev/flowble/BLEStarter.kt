package com.arstagaev.flowble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Build
import android.util.Log
import com.arstagaev.flowble.PermissionEva.Companion.requestToPermission
import com.arstagaev.flowble.enums.*
import com.arstagaev.flowble.enums.Delay
import com.arstagaev.flowble.gentelman_kit.hasPermission
import com.arstagaev.flowble.gentelman_kit.logAction
import com.arstagaev.flowble.models.CharacterCarrier
import com.arstagaev.liteble.models.ScannedDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed


class BLEStarter(ctx : Context) {
    private val TAG = "BLEStarter"
    var bleActions: BleActions? = null
    private var lastSuccess = false
    private var internalContext: Context? = ctx
    //private var coroutineContext: Context? = null
    var btAdapter: BluetoothAdapter? = null

    init {
        checkPermissions()
        bookingMachine()
        bleActions = BleActions(internalContext)
        btAdapter = bleActions?.btAdapter
        isBluetoothEnabled()
    }

    private fun bookingMachine() {
        logAction("START!!")
        CoroutineScope(Dispatchers.Default).async {
            bleCommandTrain.collectIndexed { index, operation ->
                async {

                    operation.forEachIndexed { index, bleOperations ->
                        // check permissions:
                        lastSuccess = isBluetoothEnabled()
                        println(">>> start operation[ ${bleOperations.toString()}")
                        lastSuccess = selector(bleOperations) ?: false

                        if (!lastSuccess && bleOperations.isImportant) {
                            // force waiting finish of operation
                            while (!lastSuccess) {

                                if (isBluetoothEnabled()) {
                                    logAction("repeat: ${bleOperations}")
                                    lastSuccess = selector(bleOperations) ?: false
                                }

                                delay(3000)
                            }
                        }

                    }
                    println(">>> end operation ]")

                }.await()
            }
        }
    }

    fun isBluetoothEnabled(): Boolean {
        var result = true
        CoroutineScope(CoroutineName("permission")).launch {
            if (bleActions?.btAdapter?.isEnabled == false) {
                requestToPermission.emit(BlePermissions.BLUETOOTH_ON)
                result = false
            }
        }

        return result
    }



    @Synchronized // really need?
    private suspend fun selector(operation: BleOperation) : Boolean? {
        //TODO: need check permission
        //println("$TAG ${operation} <-> ${operation.toString()}")
        logAction("New Operation: ${operation.toString()}")

        when(operation) {
            is StartScan -> with(operation) {
                return bleActions?.startScan(scanFilter)
            }
            is StopScan -> with(operation) {
                return bleActions?.stopScan()
            }

            is Connect -> with(operation) {

                return bleActions?.connectTo(address ?: "")
            }
            is Disconnect -> with(operation) {
                return bleActions?.disconnectFromDevice()
            }
            is DiscoveryServices -> with(operation) {
                //TODO not implemented
            }

            is WriteToCharacteristic -> with(operation) {
                return bleActions?.writeCharacteristic(uuid = characteristicUuid, payload = payload)
            }
            is ReadFromCharacteristic -> with(operation) {
                return bleActions?.readCharacteristic(characteristicUuid = characteristicUuid)
            }

            is EnableNotifications -> with(operation) {
                return bleActions?.enableNotifications(uuid = characteristicUuid)
            }
            is DisableNotifications -> with(operation) {
                return bleActions?.disableNotifications(uuid = characteristicUuid)
            }

            is GetBatteryLevel -> with(operation) {
                //TODO not implemented
                return false
            }
            is ForceStop -> with(operation) {
                return bleActions?.disableBLEManager()
            }
            is Delay -> with(operation) {
                delay(duration ?: 0)
                return true
                //shared_1.resetReplayCache() // ?
            }
        }
        return false
    }

    fun checkPermissions(): Boolean {
        //check permissions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false
                && internalContext?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false
                && internalContext?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false) {

                //REVERT_WORK_CAUSE_PERMISSION = false
                return true
            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"# BLUETOOTH_SCAN:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false} #")
                Log.e(TAG,"# BLUETOOTH_CONNECT:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false} #")
                Log.e(TAG,"########################################")
                return false
                //REVERT_WORK_CAUSE_PERMISSION = true
            }
        }else {
            if (internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false) {

                //REVERT_WORK_CAUSE_PERMISSION = false
                return true
            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"########################################")
                return false
                //REVERT_WORK_CAUSE_PERMISSION = true
            }
        }
    }

    suspend fun forceStop() = bleActions?.disableBLEManager()

    companion object {
        var bleCommandTrain = MutableSharedFlow<MutableList<BleOperation>>(5,0, BufferOverflow.SUSPEND)

        var scanDevices = MutableStateFlow(arrayListOf<ScannedDevice>())

        var outputBytesNotifyIndicate = MutableSharedFlow<CharacterCarrier>(10,0, BufferOverflow.SUSPEND)
        var outputBytesRead           = MutableSharedFlow<CharacterCarrier>(10,0, BufferOverflow.SUSPEND)

        var servicesCharacteristics   = MutableSharedFlow<MutableList<CharacterCarrier>>(10,0, BufferOverflow.SUSPEND)
        // setup in activity, chain with him by lifecycle and run if needed:
        //var requestToPermission = MutableSharedFlow<BlePermissions>(5,0, BufferOverflow.SUSPEND)
    }
}