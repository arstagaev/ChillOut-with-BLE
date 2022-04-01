package com.arstagaev.backble.ble.broadcastreceivers

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.util.Log
import com.arstagaev.chilloutble.ble.control_module.BleService
import com.arstagaev.chilloutble.ble.control_module.enum.ActionOfService


class CloseServiceReceiverRecorderLogs : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ccc","ccc  START!!!!!")
        //log("###############Start !!!!!!!!!!!!!!!!")

        //context.sendBroadcast(Intent("call"));
//        when(intent.action) {
//            is Actions -> {
//                print("")
//            }
//            is String -> {
//                print("")
//            }
//        }
        Intent(context, BleService::class.java).also {
            it.action = ActionOfService.STOP.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i("CloseServiceReceiverRecorderLogs","### Starting the service in >=26 Mode")
                context.startForegroundService(it)
                return
            }else {
                Log.i("CloseServiceReceiverRecorderLogs","### Starting the service in < 26 Mode")
                context.startService(it)
            }

        }

//        Intent(context, BleService::class.java).also {
//            it.action = ActionOfService.STOP.name
//
//           //Unbonding
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                //log("Starting the service in >=26 Mode")
//                context.startForegroundService(it)
//                return
//            }
//
//            //log("Starting the service in < 26 Mode")
//            context.startService(it)
//        }



        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
//        val action = intent.getStringExtra("action")
//        if (action == "action1") {
//            performAction1()
//        } else if (action == "action2") {
//            performAction2()
//        }
//        //This is used to close the notification tray
//        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//        context.sendBroadcast(it)
    }


}