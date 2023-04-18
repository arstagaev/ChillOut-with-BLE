package com.arstagaev.chilloutble

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.arstagaev.chilloutble.ui.theme.ChillOutBLETheme
import com.arstagaev.flowble.*
import com.arstagaev.flowble.enums.*
import com.arstagaev.flowble.gentelman_kit.bytesToHex
import com.arstagaev.flowble.extensions.hasPermission
import com.arstagaev.flowble.gentelman_kit.logWarning
import com.arstagaev.flowble.extensions.requestPermission
import kotlinx.coroutines.*
import java.util.*

class MainActivity : ComponentActivity() {


    var bleStarter : BLEStarter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleStarter = BLEStarter.getInstance(this).also {
            it.showOperationToasts = true // show logs in Toast
        }

        // here we receive bytes from Notify characteristic:
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            BLEStarter.outputBytesNotifyIndicate.collect {
                logWarning("Result: ${bytesToHex(it.value!!)} ${BleParameters.STATE_BLE}")
            }
        }

        // here we get found devices after scanning:
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            BLEStarter.scanDevices.collect {
                logWarning("What I found: ${it}")
            }
        }

        setContent {
            ChillOutBLETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Box(modifier = Modifier
                        .fillMaxSize()) {
                        Column(modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(90.dp)
                                    .padding(vertical = 10.dp)
                                    .clickable { },colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                                , onClick = {

                                    if (isAllPermissionsEnabled()) {
                                        launchLib()
                                    }

                                }
                            ) {
                                Text(modifier = Modifier, text = "Start Chill Out",textAlign = TextAlign.Center, color = Color.White)
                            }
                            Button(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(90.dp)
                                    .padding(vertical = 10.dp)
                                    .clickable { },colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                                , onClick = {

                                    if (isAllPermissionsEnabled()) {
                                        stopLib()
                                    }

                                }
                            ) {
                                Text(modifier = Modifier, text = "Stop Chill Out",textAlign = TextAlign.Center, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // How to use library:
    private fun launchLib() {
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            BLEStarter.bleCommandTrain.emit(mutableListOf(
                StartScan(),
                Retard(6000L),
                Connect("44:44:44:44:44:0C"),
                Retard(1000L),
                StopScan(),
                ReadFromCharacteristic("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")),
                Retard(1000L),
                EnableNotifications("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8")),
                Retard(1000L),
                DisableNotifications("44:44:44:44:44:0C", characteristicUuid = UUID.fromString("beb54202-36e1-4688-b7f5-ea07361b26a8"))
            ))
        }
    }

    private fun stopLib() {
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            bleStarter?.forceStop()
        }
    }

    private fun isAllPermissionsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                requestPermission(Manifest.permission.BLUETOOTH_CONNECT,1)
                return false
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                requestPermission(Manifest.permission.BLUETOOTH_SCAN,2)
                return false
            }
        }

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,3)
            return false
        }
        return true
    }

}
