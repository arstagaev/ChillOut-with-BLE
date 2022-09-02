package com.arstagaev.flowble

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed

class BLEStarter(ctx : Context) {
    private val TAG = "BLEStarter"
    var bleActions: BleActions? = null

    init {
        observer()
        bleActions = BleActions(ctx)
    }

    private fun observer() {
        CoroutineScope(Dispatchers.Default).async {
            shared_1.collectIndexed { index, operation ->
                var a = async {

                    operation.forEachIndexed { index, bleOperations ->
                        println(">>> start operation[ ${bleOperations.name}")
                        selector(bleOperations)
                    }
                    println(">>> end operation ]")

                }.await()
            }
        }
    }

    fun letsGO() {

    }

    var bleSuccess = false

    @Synchronized // really need?
    private suspend fun selector(operation: BleOperations) : Boolean? {
        //TODO: need check permission
        println("$TAG ${operation.name} <-> ${operation.toString()}")

        when(operation) {
            BleOperations.START_SCAN -> {
                return bleActions?.startScan()
            }
            BleOperations.STOP_SCAN -> {
                return bleActions?.stopScan()
            }
            BleOperations.ADVERTISE -> {
                //TODO not implemented
            }
            BleOperations.CONNECT -> {
                return bleActions?.connectTo(operation.macAddress ?: "")
            }
            BleOperations.DISCONNECT -> {
                return bleActions?.disconnectFromDevice()
            }
            BleOperations.DISCOVERY_SERVICES -> {}
            BleOperations.WRITE_TO_CHARACTERISTIC -> {
                return bleActions?.writeCharacteristic(uuid = operation.uuid!!, payload = operation.byteArray!!)
            }
            BleOperations.READ_FROM_CHARACTERISTIC -> {
                return bleActions?.readCharacteristic(uuid = operation.uuid!!)
            }
            BleOperations.ENABLE_NOTIFY_INDICATE -> {
                return bleActions?.enableNotifications(uuid = operation.uuid!!)
            }
            BleOperations.DISABLE_NOTIFY_INDICATE -> {
                return
            }
            BleOperations.GET_BATTERY_LEVEL -> {

            }
            BleOperations.FULL_STOP -> {
                bleActions?.disableBLEManager()
            }
            BleOperations.DELAY -> {
                delay(operation.duration ?: 0)
                shared_1.resetReplayCache()
            }
        }
        return false
    }

    companion object {
        var shared_1 = MutableSharedFlow<MutableList<BleOperations>>(5,0, BufferOverflow.SUSPEND)
        var outputBytes = MutableStateFlow(byteArrayOf())
        var outputBytesRead = MutableStateFlow(byteArrayOf())
        var servicesCharacteristics = MutableStateFlow<List<BluetoothGattCharacteristic>>(listOf())
    }
}