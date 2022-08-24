package com.arstagaev.chilloutble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arstagaev.chilloutble.ui.theme.ChillOutBLETheme
import com.arstagaev.liteble.BleActions
import com.arstagaev.liteble.BleParameters
import com.arstagaev.liteble.gentelman_kit.requestPermission
import com.arstagaev.liteble.models.ScannedDevice
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    //val bluetoothManager = getSystemService<BluetoothManager>()
    //?: throw IllegalStateException("BluetoothManager not found")


    // From the previous section:
    lateinit var bluetoothManager : BluetoothManager
    lateinit var btAdapter: BluetoothAdapter
    //val bluetoothLeScanner by lazy {  btAdapter.bluetoothLeScanner }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        btAdapter = bluetoothManager.getAdapter()
        bluetoothLeScanner = btAdapter!!.bluetoothLeScanner


        val bleActions = BleActions(this)

        bleActions.bluetoothGattCallback =  object : BluetoothGattCallback() {
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)

            }
        }

        requestPermission(Manifest.permission.BLUETOOTH_CONNECT,4)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermission(Manifest.permission.BLUETOOTH_SCAN,3)
        }
        CoroutineScope(Dispatchers.Default).launch {
            async {
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,1)
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,2)

                //requestPermission(Manifest.permission.BLUETOOTH_CONNECT,4)
            }.await()
           // bleAction.startScan()
        }

        setContent {
            ChillOutBLETheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp).clickable {  },colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                            ,onClick = {
                                var bleAction = BleActions(applicationContext)
                                bleAction.startScan()
                            }
                        ) {

                        }
                    }

                }
            }
        }

        //scanLeDevice()
        //bluetoothLeScanner.startScan(leScanCallback)
    }
    private lateinit var bluetoothLeScanner :BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
        print("IS Scan : ${scanning}")
    }
    private var leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.i("TAG","onScanResult: ${errorCode}")
        }


        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i("www","www ${BleParameters.scanResults.value.joinToString()}  || [${result.device.address}]")
            Log.i("TAG","onScanResult: ${result.rssi}")

            val indexQuery = BleParameters.scanResults.replayCache.last().indexOfFirst {
                it.bt.address == result.device.address
            }
            /** A scan result already exists with the same address */
            if (indexQuery != -1) {
                // for closest connect
                // refreshing is about 20-60 sec rssi
                for (i in 0 until BleParameters.scanResults.replayCache.last().size) {
                    if (BleParameters.scanResults.replayCache.last()[i].bt.address == result.device.address) {
                        BleParameters.scanResults.value[i].rssi = result.rssi

                    }
                }



            } else { /** founded new device */
                BleParameters.scanResults.value.add(ScannedDevice(result.device,result.rssi))
                BleParameters.scanResults.value.sortByDescending { it.rssi } // sort by rssi found devices
                //Log.i(TAGx,"Found BLE device!!! Name: ${result.device.name ?: "Unnamed"}, address: ${result.device.address}")
//                if (result.device.name != null && result.device.name.toString().contains(TARGET_PART_OF_NAME,true) == true) {
//
//                }

            }
        }
    }

}
