package com.arstagaev.flowble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.arstagaev.flowble.enums.BlePermissions
import com.arstagaev.flowble.gentelman_kit.hasPermission
import com.arstagaev.flowble.gentelman_kit.logAction
import com.arstagaev.flowble.gentelman_kit.requestPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectIndexed

class PermissionEva(componentActivity: ComponentActivity) : ComponentActivity() {

    val compActivity: ComponentActivity = componentActivity
    private val requestMultiplePermissions =
        componentActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
                //requestPermission(it.key,1)
//                if (checkPermissions()) {
//                    ALL_PERMISSION_GRANTED.value = true
//                }else {
//                   // launch()
//                }
            }
        }


    fun isAllPermissionsAllowed(): Boolean {
        var result = true
        CoroutineScope(CoroutineName("permission")).launch {
//            if (!compActivity.hasPermission(BlePermissions.BLUETOOTH_ON.name)) {
//                requestToPermission.emit(BlePermissions.BLUETOOTH_ON)
//                result = false
//            }

//            if (!compActivity.hasPermission(BlePermissions.LOCATION.value)) {
//                requestToPermission.emit(BlePermissions.LOCATION)
//                result = false
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (!compActivity.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//                    requestToPermission.emit(BlePermissions.BLUETOOTH_CONNECT)
//                    result = false
//                }
//
//                if (!compActivity.hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
//                    requestToPermission.emit(BlePermissions.BLUETOOTH_SCAN)
//                    result = false
//                }
//
//            }
            logAction("BLE permissions is allowed: $result")


        }
        return result

    }

    fun runner() {
        CoroutineScope(CoroutineName("opp")).async {
            println(">>>>> 0000")
            PermissionEva.requestToPermission.collectIndexed { index, value ->
                println(">>>>> 1111  ${value}")
                if (value == BlePermissions.BLUETOOTH_ON) {

                    if (compActivity.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        val blueToothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        ActivityCompat.startActivityForResult(compActivity, blueToothIntent, 1,null)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            compActivity.requestPermission(Manifest.permission.BLUETOOTH_CONNECT, 2)
                        }
                    }

                } else {
                    async {
                        //requestMultiplePermissions.launch()
                        compActivity.requestPermission(value.value, (0..100).random())
                        delay(4000)
                    }.await()

                    if (!compActivity.hasPermission(value.value)) {
                        PermissionEva.requestToPermission.emit(value)
                        delay(100)

                    }


                }





                println(">>>>> 2222")
            }

            println(">>>>> 3333")

        }
    }

    fun close() {

    }

    companion object {
        var requestToPermission = MutableSharedFlow<BlePermissions>(5,1, BufferOverflow.SUSPEND)
    }
}