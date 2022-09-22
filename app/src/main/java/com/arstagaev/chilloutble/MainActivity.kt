package com.arstagaev.chilloutble

import android.Manifest
import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.arstagaev.chilloutble.ui.theme.ChillOutBLETheme
import com.arstagaev.flowble.*
import com.arstagaev.flowble.enums.*
import com.arstagaev.flowble.enums.Delay
import com.arstagaev.flowble.gentelman_kit.bytesToHex
import com.arstagaev.flowble.gentelman_kit.logWarning
import com.arstagaev.flowble.gentelman_kit.requestPermission
import kotlinx.coroutines.*
import java.util.UUID

class MainActivity : ComponentActivity() {


    var bleStarter : BLEStarter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        bluetoothManager = getSystemService(BluetoothManager::class.java)
//        btAdapter = bluetoothManager.getAdapter()
//        bluetoothLeScanner = btAdapter!!.bluetoothLeScanner
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, (0..100).random())
        //requestPermission(Manifest.permission.BLUETOOTH_CONNECT, 1)

        //val blueToothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        //ActivityCompat.startActivityForResult(this, blueToothIntent, 1,null)

        PermissionBlock(this).launch()
        bleStarter = BLEStarter(this)

        CoroutineScope(lifecycleScope.coroutineContext).launch {
            BLEStarter.bleCommandTrain.emit(mutableListOf(
                StartScan(),
                Delay(6000L),
                Connect("44:44:44:44:44:0C", isImportant = true),
                StopScan(),
                //ReadFromCharacteristic("44:44:44:44:44:0C",UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"), isImportant = true)
                EnableNotifications("44:44:44:44:44:0C",UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"), isImportant = true),
                EnableNotifications("44:44:44:44:44:0C",UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"), isImportant = true)
                //BleOperations.DELAY.also { it.duration = 1000 },
            ))
        }

        CoroutineScope(lifecycleScope.coroutineContext).launch {
            BLEStarter.outputBytesNotifyIndicate.collect {
                logWarning("Result: ${bytesToHex(it.value!!)}")
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestMultiplePermissions.launch(arrayOf(
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.BLUETOOTH_CONNECT))
//        }


        setContent {
            ChillOutBLETheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clickable { },colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                            , onClick = {
                                val intent = Intent(this@MainActivity,MainActivity2::class.java)
                                startActivity(intent)
                                CoroutineScope(CoroutineName("stop")).launch {
                                    bleStarter?.forceStop()
                                }
                                finish()
//                                var bleAction = BleActions(applicationContext)
//                                bleAction.startScan()
                            }
                        ) {

                        }
                    }
//                    var asd = scanDevices.collectAsState()
//
//                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
//                        GlobalScope.launch {
//
//                                //println("haha ${asd.value.size}")
////                                item(1) {
////                                    Text(text = "name: it.bt.address} || it.bt.name}", style = TextStyle(fontSize = 20.sp))
////                                }
//                                items(asd.value) {
//                                    Text(text = "name: ${it.bt.address} || ${it.bt.name}", style = TextStyle(fontSize = 20.sp))
//                                }
//
//
//
//                        }
//
//
//                    }
                    //requestPermission(Manifest.permission.BLUETOOTH_CONNECT,4)
                    //requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1)
                    //requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,2)
//                    requestPermission(Manifest.permission.BLUETOOTH_SCAN,1)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//
//                    }
//                    LaunchedEffect(key1 = bl_connect.value) {
//
//
//                    }
//                    //bl_connect = true
//                    LaunchedEffect(key1 = bl_location, key2 = bl_connect_already) {
//                        CoroutineScope(Dispatchers.Default).launch {
//                            async {
//                                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1)
//                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,2)
//
//                                //requestPermission(Manifest.permission.BLUETOOTH_CONNECT,4)
//                            }.await()
//                            // bleAction.startScan()
//                        }
//                    }

                }
            }
        }
    }

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    override fun onDestroy() {

        super.onDestroy()

    }
}
