package com.arstagaev.chilloutble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstagaev.chilloutble.ui.theme.ChillOutBLETheme
import com.arstagaev.flowble.*
import com.arstagaev.flowble.BLEStarter.Companion.scanDevices
import com.arstagaev.flowble.gentelman_kit.requestPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    //val bluetoothManager = getSystemService<BluetoothManager>()
    //?: throw IllegalStateException("BluetoothManager not found")


    // From the previous section:
    lateinit var bluetoothManager : BluetoothManager
    lateinit var btAdapter: BluetoothAdapter
    //val bluetoothLeScanner by lazy {  btAdapter.bluetoothLeScanner }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        bluetoothManager = getSystemService(BluetoothManager::class.java)
//        btAdapter = bluetoothManager.getAdapter()
//        bluetoothLeScanner = btAdapter!!.bluetoothLeScanner

        PermissionBlock(this@MainActivity).launch()

        //bleStarter.letsGO()
        if (true) {
            val bleStarter = BLEStarter(this)
            GlobalScope.launch {
                BLEStarter.shared_1.emit(mutableListOf(
                    BleOperations.START_SCAN,
                    //BleOperations.DELAY.also { it.duration = 1000 },

                ))
//            BLEStarter.shared_1.emit(mutableListOf(
//                BleOperations.CONNECT,
//                BleOperations.DELAY.also { it.duration = 1000 },
//                BleOperations.DELAY.also { it.duration = 1000 },
//                BleOperations.DELAY.also { it.duration = 1000 },
//                BleOperations.DISCONNECT.also { it.duration = 1000 }
//            ))
            }
        }

        var bl_connect = mutableStateOf(false)
        var bl_connect_already = false
        var bl_location = false
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
                                var bleAction = BleActions(applicationContext)
                                bleAction.startScan()
                            }
                        ) {

                        }
                    }
                    var asd = scanDevices.collectAsState()

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        GlobalScope.launch {

                                //println("haha ${asd.value.size}")
//                                item(1) {
//                                    Text(text = "name: it.bt.address} || it.bt.name}", style = TextStyle(fontSize = 20.sp))
//                                }
                                items(asd.value) {
                                    Text(text = "name: ${it.bt.address} || ${it.bt.name}", style = TextStyle(fontSize = 20.sp))
                                }



                        }


                    }
                    //requestPermission(Manifest.permission.BLUETOOTH_CONNECT,4)
                    //requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1)
                    //requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,2)
                    requestPermission(Manifest.permission.BLUETOOTH_SCAN,1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    }
                    LaunchedEffect(key1 = bl_connect.value) {


                    }
                    //bl_connect = true
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
        //frop(this)
        //scanLeDevice()
        //bluetoothLeScanner.startScan(leScanCallback)
    }

    private fun frop(activity: Activity) {
        TODO("Not yet implemented")
    }

    private lateinit var bluetoothLeScanner :BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000


}
