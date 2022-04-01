package com.arstagaev.chilloutble.ble.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.arstagaev.backble.ble.enums.Actions
import com.arstagaev.chilloutble.ble.control_module.BleService
import com.arstagaev.chilloutble.ble.control_module.ServiceState
import com.arstagaev.chilloutble.ble.control_module.getServiceState

class StartReceiver : BroadcastReceiver() {
    // for notifications
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, BleService::class.java).also {
                it.action = Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("StartReceiver","Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                Log.i("StartReceiver","Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }
}
