package com.arstagaev.flowble

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectIndexed

class BLEStarter(ctx : Context) {
    val TAG = "BLEStarter"

    init {
        observer()
    }

    private fun observer() {
        CoroutineScope(Dispatchers.Default).async {
            shared_1.collectIndexed { index, operation ->
                async {

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

    @Synchronized // really need?
    private suspend fun selector(operation: BleOperations) {

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

            BleOperations.DELAY -> {
                delay(operation.duration ?: 0)
            }
        }
    }

    companion object {
        var shared_1 = MutableSharedFlow<MutableList<BleOperations>>(5,0, BufferOverflow.SUSPEND)
    }
}