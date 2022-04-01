package com.arstagaev.chilloutble.ble.control_module

//import com.avtelma.backblelogger.rawparser.tools.VariablesAndConst.CURRENT_FILE_IN_PROGRESS

//import com.avtelma.backgroundparser.core.algorithm.*
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.arstagaev.chilloutble.ble.BLEParameters.ACTION_NOW_SERVICE
import com.arstagaev.chilloutble.ble.BLEParameters.CONNECTED_DEVICE
import com.arstagaev.chilloutble.ble.BLEParameters.CURRENT_SERVICE_CONNECT_STYLE
import com.arstagaev.chilloutble.ble.BLEParameters.STATE_NOW_SERVICE
import com.arstagaev.chilloutble.ble.BLEParameters.TARGET_CONNECT_ADDRESS
import com.arstagaev.chilloutble.ble.BLEParameters.isADVERTISING_WORK
import com.arstagaev.chilloutble.ble.BLEParameters.scanResultsX
import com.arstagaev.backble.ble.broadcastreceivers.CloseServiceReceiverRecorderLogs
import com.arstagaev.chilloutble.ble.broadcastreceivers.UnBondingReceiver
import com.arstagaev.backble.ble.enums.StateOfService
import com.arstagaev.backble.ble.enums.StyleOfConnect
import com.arstagaev.backble.core.CoreParameters.RECORD_ACTIVITY
import com.arstagaev.backble.core.CoreParameters.STARTING_DELAY
import com.arstagaev.backble.core.CoreParameters.TIME_OF_TRIP
import com.arstagaev.chilloutble.R
import com.arstagaev.backble.ble.BLEActions
import com.arstagaev.chilloutble.ble.control_module.enum.ActionOfService
import com.arstagaev.backble.gentelman_kit.bytesToHex
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.takeWhile
import java.io.*


/**
 * Need check permission before run this Service
 *
 */

@SuppressLint("MissingPermission")
class BleService : Service() {
    private val TAG = "BleService"
    private var PERCENTAGE_OF_COMPLETE = 0
    private var wakeLock: PowerManager.WakeLock? = null
    private var isBleServiceStarted = false
    private var bleAction : BLEActions? = null


    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null

        return null
    }

    /**
     * ENTRY-COMMAND POINT OF SERVICE
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")

        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                ActionOfService.START.name ->  {
                    ACTION_NOW_SERVICE =  ActionOfService.START
                    startService() // Engine Start
                }
                ActionOfService.STOP.name  ->  {
                    ACTION_NOW_SERVICE =  ActionOfService.STOP
                    stopService()
                }
                ActionOfService.UNBOND.name  ->  {
                    ACTION_NOW_SERVICE =  ActionOfService.UNBOND
                    startService()
                }
                ActionOfService.ADVERTISE_SCAN.name -> {
                    ACTION_NOW_SERVICE =  ActionOfService.ADVERTISE_SCAN
                    startService()
                }
            }

        } else {
            log("with a null intent. It has been probably restarted by the system.")
        }


        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }



    // Send an Intent with an action named "my-event".
//    private fun sendMessage(msg : String) {
//        val intent = Intent("my-event")
//        // add data
//        intent.putExtra("message", msg)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }

    override fun onCreate() {
        super.onCreate()
        log("The parsing service has been created".toUpperCase())
        if (!isBleServiceStarted) {
            val notification = createNotification()
            startForeground(2, notification)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())

        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, BleService::class.java).also {
            it.setPackage(packageName)
        };
        var restartServicePendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // FLAG_MUTABLE
            restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE);

        }else {
            restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);

        }
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
    }




    @SuppressLint("CheckResult")
    private fun startService() {
        if (isBleServiceStarted) { return }

        log("Starting the foreground service task")

        isBleServiceStarted = true


        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ParsingEventService::lock").apply {
                    acquire()
                }
            }


        toastShow("Service has been started",this@BleService)

        //initDefineLocation()



        bleAction = BLEActions(this@BleService)
//        CoroutineScope((Dispatchers.Main)).launch {
//            while (isServiceStarted) {
//                Log.w("Mmm","mmm ")
//                delay(1000)
//            }
//        }
        refreshNotification()

        /**
         * MAIN LOOPER
         */

        MainLooper()
        AdviserLooper()
        //test

//        var timer = object : CountDownTimer(31000,1000){
//            override fun onTick(millisUntilFinished: Long) {
//                notifyMe()
//            }
//
//            override fun onFinish() {
//
//            }
//        }.start()
    }

    private fun AdviserLooper() {
        CoroutineScope((Dispatchers.Main)).launch {
            delay(100)
            while (isBleServiceStarted) {
                Log.i("Adviser Ins","Adviser Inspector isADVERTISING_WORK:${isADVERTISING_WORK}")
                if (isADVERTISING_WORK){
                    bleAction?.startAdvertisingScan()
                }else {
                    delay(14000)
                    bleAction?.stopAdvertisingScan()
                }

                delay(1000)
                // HERE scan always be false, need check logs in startAdv.. method above
                //Log.i("Adviser Ins","Adviser Inspector isADVERTISING_WORK:${isADVERTISING_WORK}. isScanningAdvertise: ${bleAction?.advScanning}   isScanningDefault: ${bleAction?.scanning ?: "null"} ")
            }
        }

    }

    private fun MainLooper() {
        CoroutineScope((Dispatchers.Main)).launch {
            delay(STARTING_DELAY)
            Log.i(TAG,"Start LOOPER<<<<<<<<<<<<<<<<<<<<<<<")
            while (isBleServiceStarted) {

                when(ACTION_NOW_SERVICE) {
                    ////////////////////////
                    ActionOfService.START -> {

                        when(STATE_NOW_SERVICE) {
                            StateOfService.NEUTRAL -> {
                                bleAction?.disconnectFromDevice()
                                delay(1000)
                            }
                            StateOfService.NO_CONNECTED, StateOfService.INIT, StateOfService.LOSS_CONNECTION_AND_WAIT_NEW -> {
                                // trying to connect
                                connectionBlock()
                            }
                            StateOfService.CONNECTING,StateOfService.DISCONNECTING,StateOfService.UNBONDING -> { // wait
                                delay(1000)
                            }
                            StateOfService.CONNECTED_BUT_NO_SUBSCRIBED -> {

                                try {
                                    bleAction!!.manageNotifyIndicate(isChecked = true)
                                } catch (e: Exception) {
                                    Log.e("ERROR","ERROR CONNECTED_BUT_NO_SUBSCRIBED: ${e.message}")
                                }

                                delay(2000)

                                if (STATE_NOW_SERVICE == StateOfService.NO_CONNECTED) {
                                    Log.w("tag","emergency reset, coz forgot bond !!")
                                    bleAction!!.connectTo(scanResultsX[0].bt.address)
                                    delay(4000)
                                    bleAction!!.unBonding(isEmergencyReset = true)
                                    STATE_NOW_SERVICE =StateOfService.NO_CONNECTED
                                }
                                // work with characteristics/services
                                Log.i("tag","${STATE_NOW_SERVICE.name}")
                            }
                            StateOfService.NOTIFYING_OR_INDICATING -> {
                                // receive/parse data
                                TIME_OF_TRIP++
                            }
                        }
                    }
                    ////////////////////////
                    ActionOfService.ADVERTISE_SCAN -> {
                        delay(1000)
                    }
                    ////////////////////////
                    ActionOfService.UNBOND -> {

                        when(STATE_NOW_SERVICE) {
                            StateOfService.NO_CONNECTED, StateOfService.INIT, StateOfService.LOSS_CONNECTION_AND_WAIT_NEW -> {
                                // trying to connect
                                connectionBlock()
                            }
                            StateOfService.CONNECTING,StateOfService.DISCONNECTING,StateOfService.UNBONDING -> { // wait
                                delay(4000)
                            }
                            StateOfService.CONNECTED_BUT_NO_SUBSCRIBED -> {
                                delay(1000)
                                bleAction!!.unBonding(isEmergencyReset = false)
                                delay(5000)
                                stopService()
                            }
                            StateOfService.NOTIFYING_OR_INDICATING -> { // receive/parse data
                            }
                        }
                    }
                    ////////////////////////
                }

                delay(1000)

                Log.w("sss","<><><><><><><> LOAD SERVICE AGAIN <><><><><>${ACTION_NOW_SERVICE.name} || ${STATE_NOW_SERVICE.name} || ${CURRENT_SERVICE_CONNECT_STYLE.name} isScan:[${bleAction?.scanning ?: "null"}] ${CONNECTED_DEVICE?.name ?: "null"} [${CONNECTED_DEVICE?.address ?: "null"}] ")
            }
            log("End of the loop for the service")
        }
    }

    private suspend fun connectionBlock() {
//        if (CURRENT_SERVICE_STATE != StateOfService.NO_CONNECTED
//            || CURRENT_SERVICE_STATE != StateOfService.LOSS_CONNECTION_AND_WAIT_NEW
//            || CURRENT_SERVICE_STATE != StateOfService.INIT) {
//            return
//        }


        when(CURRENT_SERVICE_CONNECT_STYLE) {
            StyleOfConnect.TARGET -> {
                if (TARGET_CONNECT_ADDRESS != null || TARGET_CONNECT_ADDRESS != "") {

                    if (bleAction!!.connectTo(TARGET_CONNECT_ADDRESS)) { } else {
                        delay(2000)
                    }
                    delay(3400)

                }else {
                    toastShow("TARGET_CONNECT_ADDRESS = null !",this@BleService)
                }
            }
            StyleOfConnect.BY_SCAN_BY_CLOSEST -> {
                Log.i(TAG,"startScan ${scanResultsX.joinToString()}")
                bleAction!!.startScan()

                delay(7000)

                if (scanResultsX.isNotEmpty()) {
                    scanResultsX.forEach {
                        Log.i("ble","ble~>${it.bt.name} [${it.bt.address}]  rssi:${it.rssi}")
                    }
                    bleAction!!.connectTo(scanResultsX[0].bt.address)
                    delay(4000)
                    scanResultsX.clear()
                }
                Log.i(TAG,"after startScan and try connect ${scanResultsX.joinToString()}")
            }
            StyleOfConnect.BY_BOND -> {  }
        }
    }

    fun appendText(sFileName: String, sBody: String){
        try {
            val root = File(Environment.getExternalStorageDirectory(), "ItelmaBLE_Background/Jsons")
            if (!root.exists()) {
                root.mkdirs()
            }
            val file = File(root, sFileName)

            val fileOutputStream = FileOutputStream(file,true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.append("\n "+sBody)

            outputStreamWriter.close()
            fileOutputStream.close()
            //findAndReplacePartOfText(file)

        } catch (e: IOException) {
            Log.e("ccc","ERROR "+ e.message)
            e.printStackTrace()
        }

    }
    //msg: String, isFirstNotif : Boolean
    fun refreshNotification() {
        //bleAction!!.bluetoothGattCallback.onCharacteristicChanged()

        // only notification I refresh, dont write to file
        CoroutineScope(Dispatchers.Main).launch {
            bleAction?.updateNotificationFlow
                ?.takeWhile { STATE_NOW_SERVICE == StateOfService.NOTIFYING_OR_INDICATING }
                ?.collect {
                    Log.i("fff","fff ${bytesToHex(it)}")

                }


        }

//        if (isFirstNotif){
//
//            builder2.setContentTitle("${msg}")
//            builder2.setContentText("")
//            notificationManager.notify(notificationChannelId2,2,builder2.build())
//
//        }else{
//
//            //CURRENT_LOG_JUST_PRESENTATION = msg
//            builder2.setContentTitle("Log:")
//            builder2.setContentText("$msg spd: $CURRENT_SPEED stl: ${CURRENT_SERVICE_CONNECT_STYLE.name.take(3)}|${TYPE_OF_INPUT_LOG.name}")
//            notificationManager.notify(notificationChannelId2,2,builder2.build())
//
//        }
    }

    /////////////


    private fun stopService() {

        log("Stopping the foreground BLEservice  X_X ")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            bleAction?.updateNotificationFlow = emptyFlow()
            bleAction?.disableBLEManager()
            STATE_NOW_SERVICE = StateOfService.INIT

            delay(400)
            //Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
            try {
                wakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                    }
                }
                stopForeground(true)
                stopSelf()
            } catch (e: Exception) {
                log("Service stopped without being started: ${e.message}")
            }
            isBleServiceStarted = false
            setServiceState(this@BleService, ServiceState.STOPPED)
        }
    }

    private lateinit var notificationManager : NotificationManager
    private val notificationChannelId  = "ble_lib1"
    private val notificationChannelId2 = "ble_lib2"

    private var builder = NotificationCompat.Builder(this, notificationChannelId)
        .setContentTitle("AVTelma")
        .setContentText("\uD83D\uDD34 Working..")
        // .setContentIntent(pendingIntent)
        //.setTicker("Ticker text")
        .setPriority(NotificationCompat.PRIORITY_MIN) // for under android 26 compatibility
    //.setOnlyAlertOnce(true) // ATTENTION!!!
    //.addAction(actionX)

    private var builder2 = NotificationCompat.Builder(this, notificationChannelId2)
        .setContentTitle("Log:")
        .setOnlyAlertOnce(true) // ATTENTION!!!
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    private fun refreshNotification(msg: String, isFirstNotif : Boolean) {
        if (isFirstNotif){

            builder2.setContentTitle("${msg}")
            builder2.setContentText("")
            notificationManager.notify(notificationChannelId2,2,builder2.build())

        }else{

            //CURRENT_LOG_JUST_PRESENTATION = msg
            builder2.setContentTitle("Log:")
            builder2.setContentText("$msg stl: ${STATE_NOW_SERVICE.name.take(3)}|")
            notificationManager.notify(notificationChannelId2,2,builder2.build())

        }
    }


    private fun createNotification(): Notification {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // coz notif channels added in android 8.0
            val channel = NotificationChannel(
                notificationChannelId,
                "AVTelma_MainIndicatorOfWork",
                NotificationManager.IMPORTANCE_LOW
            ).let {
                it.description = "Main Indicator of Service"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 500, 100, 100, 100, 100, 100, 100, 300, 400, 500)
                it
            }

            val channel2 = NotificationChannel(
                notificationChannelId2,
                "AVTelma_xyz",
                NotificationManager.IMPORTANCE_DEFAULT
            ).let {
                it.description = "no main Indicator of Service"
                it.enableLights(true)
                it.lightColor = Color.RED
                it
            }
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channel2)
        }

        if(RECORD_ACTIVITY == null) {
            //RECORD_ACTIVITY = MainActivity::class.java
        }
        Log.i("ccc","ccclass  ${RECORD_ACTIVITY?.name}")
        var pendingIntent: PendingIntent? = null
        var actionIntent : PendingIntent? = null
        var actionIntent2: PendingIntent? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = Intent(this,RECORD_ACTIVITY).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_MUTABLE)
            }
            actionIntent = PendingIntent.getBroadcast(
                this,
                0, Intent(this, CloseServiceReceiverRecorderLogs::class.java), PendingIntent.FLAG_MUTABLE
            )
            actionIntent2 = PendingIntent.getBroadcast(
                this,
                0, Intent(this, UnBondingReceiver::class.java), PendingIntent.FLAG_MUTABLE
            )

        } else {
            actionIntent = PendingIntent.getBroadcast(
                this,
                0, Intent(this, CloseServiceReceiverRecorderLogs::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )
            actionIntent2 = PendingIntent.getBroadcast(
                this,
                0, Intent(this, UnBondingReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )

            pendingIntent = Intent(this,RECORD_ACTIVITY).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,  0)
            }

        }

        return builder
            .setContentTitle("Smart Insurance. AVTelma")
            .setContentText("\uD83D\uDD34 Working..")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            //.setTicker("Ticker text")
            .setPriority(NotificationCompat.PRIORITY_LOW) // for under android 26 compatibility
            .setOnlyAlertOnce(true) // ATTENTION!!!
            .addAction(R.drawable.ic_launcher_foreground,"stop",actionIntent)

            .build()
    }
}
