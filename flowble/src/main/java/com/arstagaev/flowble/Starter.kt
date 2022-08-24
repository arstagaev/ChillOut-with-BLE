package com.arstagaev.flowble

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class BLEStarter(ctx : Context) {
    val TAG = "BLEStarter"
    init {
        observer()
    }

    private fun observer() {
        CoroutineScope(Dispatchers.Default).async {
            shared_1.collectIndexed { index, operation ->
                async {
                    selector(operation)
                }.await()
            }
        }
    }

    fun letsGO() {

    }

    @Synchronized // really need?
    private fun selector(operation: BleOperations) {
        println("$TAG ${operation.name} <-> ${operation.toString()}")
        when(operation) {
            BleOperations.START_SCAN -> {

            }
            BleOperations.STOP_SCAN -> {

            }
            BleOperations.ADVERTISE -> {

            }

            BleOperations.CONNECT -> {

            }
            BleOperations.DISCONNECT -> {

            }

            BleOperations.DISCOVERY_SERVICES -> {

            }

            BleOperations.WRITE_TO_CHARACTERISTIC -> {

            }
            BleOperations.READ_FROM_CHARACTERISTIC -> {

            }

            BleOperations.ENABLE_NOTIFY_INDICATE -> {

            }
            BleOperations.DISABLE_NOTIFY_INDICATE -> {

            }

            BleOperations.GET_BATTERY_LEVEL -> {}

            BleOperations.DELAY -> {}
        }
    }

    companion object {
        var shared_1 = MutableSharedFlow<BleOperations>(5,0, BufferOverflow.SUSPEND)
    }
}