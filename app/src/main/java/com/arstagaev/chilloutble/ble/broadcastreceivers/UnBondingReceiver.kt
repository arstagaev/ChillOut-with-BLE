package com.arstagaev.chilloutble.ble.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.arstagaev.backble.ble.enums.Actions
import com.arstagaev.chilloutble.ble.control_module.BleService

class UnBondingReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("ccc","ccc  UNBONDING   !!!!!")
        Log.i("ccc","ccc  UNBONDING   !!!!!")
        Log.i("ccc","ccc  UNBONDING   !!!!!")
        //log("###############Start !!!!!!!!!!!!!!!!")

        Intent(context, BleService::class.java).also {
            it.action = Actions.UNBOND.name

            // //Unbonding
            // EndlessService().bleManager?.notifyCharacteristic(true, UUID.fromString("74ab521e-060d-26df-aa64-cf4df2d0d643"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //log("Starting the service in >=26 Mode")
                context.startForegroundService(it)
                return
            }

            //log("Starting the service in < 26 Mode")
            context.startService(it)
        }


//        Intent(context, EndlessService::class.java).also {
//            //it.action = Actions.STOP.name
//            context.sendBroadcast(Intent("call"));
//            //Unbonding
//            //EndlessService().bleManager?.notifyCharacteristic(true, UUID.fromString("74ab521e-060d-26df-aa64-cf4df2d0d643"))
//
//        }
    }
}
