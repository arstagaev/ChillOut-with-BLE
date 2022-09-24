package com.arstagaev.flowble

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.arstagaev.flowble.extensions.hasPermission
import kotlinx.coroutines.flow.MutableStateFlow

class PermissionBlock(componentActivity: ComponentActivity) : ComponentActivity() {

    val TAG = "PermissionBlock"
    var ctx : ComponentActivity? = null
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



    init {

    }

    fun launch() {
        if (checkPermissions()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }


    }

    fun checkPermissions(): Boolean {
        //check permissions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ctx?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false
                && ctx?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false
                && ctx?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false) {

                //REVERT_WORK_CAUSE_PERMISSION = false
                return true
            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${ctx?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"# BLUETOOTH_SCAN:${ctx?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)?: false} #")
                Log.e(TAG,"# BLUETOOTH_CONNECT:${ctx?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)?: false} #")
                Log.e(TAG,"########################################")
                return false
                //REVERT_WORK_CAUSE_PERMISSION = true
            }
        }else {
            if (ctx?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false) {

                //REVERT_WORK_CAUSE_PERMISSION = false
                return true
            }else {

                Log.e(TAG,"########################################")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# Error: Don`t have permission for BLE #")
                Log.e(TAG,"# ACCESS_FINE_LOCATION:${ctx?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)?: false} #")
                Log.e(TAG,"########################################")
                return false
                //REVERT_WORK_CAUSE_PERMISSION = true
            }
        }
    }
    companion object {
        var ALL_PERMISSION_GRANTED = MutableStateFlow(false)
    }
}