package com.arstagaev.flowble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import com.arstagaev.flowble.BleParameters.showOperationToasts
import com.arstagaev.flowble.enums.*
import com.arstagaev.flowble.enums.Retard
import com.arstagaev.flowble.extensions.hasPermission
import com.arstagaev.flowble.gentelman_kit.*
import com.arstagaev.flowble.models.CharacterCarrier
import com.arstagaev.flowble.models.ScannedDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed

private var countInitClass = 0
class BLEStarter(ctx : Context) {

    private val TAG = "BLEStarter"
    private var bleActions: BleActions? = null
    private var lastSuccess = false
    private var internalContext: Context? = ctx
    private var jobBleLifecycle = Job()


    var btAdapter: BluetoothAdapter? = null


    init {
        checkPermissions()
        bookingMachine()
        bleActions = BleActions(internalContext)
        btAdapter = bleActions?.btAdapter

        countInitClass++
        checkNumberOfInstanceThisClass()
    }

    private fun checkNumberOfInstanceThisClass() {
        if (countInitClass > 1) {
            repeat(10) {
                logError("Many times (${countInitClass}) initializing of BLEStarter class !! [Possible wrong work of BLE module]. Especially double/triple and etc. request to connect and so on")
            }
        }
    }

    private fun bookingMachine() {
        logAction("START!!")
        CoroutineScope(jobBleLifecycle + CoroutineName("Ble Starter: bookingMachine()")).async {
            bleCommandTrain.collectIndexed { index, operation ->

                async {

                    operation.forEachIndexed { index, bleOperations ->

                        async {

                            lastSuccess = selector(bleOperations) ?: run {
                                logWarning("Operation ${bleOperations.toString()} is Failed !!!")
                                false
                            }

                            if (!lastSuccess && bleOperations.isImportant) {
                                // force waiting finish of operation
                                while (!lastSuccess) {

                                    if (isBluetoothEnabled()) {
                                        logAction("repeat: ${bleOperations}")
                                        lastSuccess = selector(bleOperations) ?: run {
                                            logWarning("Operation ${bleOperations.toString()} is Failed Again !!!!")
                                            false
                                        }
                                    }

                                    delay(3000)
                                }
                            }
                        }.await()


                    }
                    logAction("End of Operation: ${operation.toString()} <<<<")

                }.await()
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return if ( bleActions?.btAdapter?.isEnabled == true ) {
            true
        }else {
            logError("Bluetooth is NOT enabled !!")
            logError("Bluetooth is NOT enabled !!")
            logError("Bluetooth is NOT enabled !!")
            logError("Bluetooth is NOT enabled !!")
            false
        }
    }



    @Synchronized
    private suspend fun selector(operation: BleOperation) : Boolean? {
        logAction("New Operation: ${operation} >>>>")

        if (!isBluetoothEnabled()) {
            delay(200)
            return false
        }

        when(operation) {
            is StartScan -> with(operation) {
                return bleActions?.startScan(scanFilter)
            }
            is StopScan -> with(operation) {
                return bleActions?.stopScan()
            }

            is Connect -> with(operation) {
                if (showOperationToasts) {
                    CoroutineScope(Dispatchers.Main).launch {
                        internalContext?.toast("New operation: ${operation}")
                    }
                }
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
            // Experimental:
            is GetBatteryLevel -> with(operation) {
                return bleActions?.getBatteryLevel()
            }
            // Experimental:
            is UnBondDeviceFromPhone -> with(operation) {
                return bleActions?.unBondDeviceFromPhone(address)
            }


            is DisableBleManager -> with(operation) {
                return bleActions?.disableBLEManager()
            }
            is Retard -> with(operation) {
                delay(duration ?: 0)
                return true
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
                return true
            }else {

                logError("$TAG ########################################")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError(TAG+" # ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                logError(TAG+" # BLUETOOTH_SCAN:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false} #")
                logError(TAG+" # BLUETOOTH_CONNECT:${internalContext?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false} #")
                logError("$TAG ########################################")
                return false
            }
        }else {
            if (internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false) {

                return true
            }else {
                logError("$TAG ########################################")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError("$TAG # Error: Don`t have permission for BLE #")
                logError(TAG+ " # ACCESS_FINE_LOCATION:${internalContext?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                logError("$TAG ########################################")
                return false
            }
        }
    }

    suspend fun forceStop() {
        if (showOperationToasts) {
            CoroutineScope(Dispatchers.Main).launch {
                internalContext?.toast("Force Stop")
            }
        }
        jobBleLifecycle.cancel()
        bleActions?.disableBLEManager()
    }

    companion object {
        var bleCommandTrain = MutableSharedFlow<MutableList<BleOperation>>(5,0, BufferOverflow.SUSPEND)

        var scanDevices = MutableStateFlow(arrayListOf<ScannedDevice>())

        var outputBytesNotifyIndicate = MutableSharedFlow<CharacterCarrier>(10,0, BufferOverflow.SUSPEND)
        var outputBytesRead           = MutableSharedFlow<CharacterCarrier>(10,0, BufferOverflow.SUSPEND)

        var servicesCharacteristics   = MutableSharedFlow<MutableList<CharacterCarrier>>(10,0, BufferOverflow.SUSPEND)
    }
}