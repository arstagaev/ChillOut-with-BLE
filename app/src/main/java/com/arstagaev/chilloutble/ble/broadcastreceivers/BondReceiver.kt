package com.arstagaev.backble.ble.broadcastreceivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

//import com.avtelma.backblelogger.storage.PreferenceMaestro


class BondReceiver : BroadcastReceiver() {
    private val TAG = "fff"
    private val BLE_PIN = "123456"
    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String = intent?.getAction().toString()
//        if (BluetoothDevice.ACTION_PAIRING_REQUEST == action) {
//            val bluetoothDevice: BluetoothDevice =
//                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
//            if (bluetoothDevice.name.toString().contains(NAME_OF_TABLET,true) == true){
//
//
//                if(BOND_FEATURE_IS){
//                    bluetoothDevice.setPin(BLE_PIN.toByteArray())
//                    //bluetoothDevice.createBond()
//                    Log.w(TAG, "Auto-entering pin: $BLE_PIN")
//                    Log.w(TAG, "pin entered and request sent...")
//                }
//
//            }else{
//                Toast.makeText(context,"It`s not Itelma tablet - please write password",Toast.LENGTH_LONG).show()
//            }

        }

    }

    fun setBluetoothPairingPin(device: BluetoothDevice) {

//        val pinBytes: ByteArray = convertPinToBytes("0000")
//        try {
//            Log.d(TAG, "Try to set the PIN")
//            val m: Method = device.javaClass.getMethod("setPin", ByteArray::class.java)
//            m.invoke(device, TAG.get)
//            Log.d(TAG, "Success to add the PIN.")
//            try {
//                device.javaClass.getMethod(
//                    "setPairingConfirmation",
//                    Boolean::class.javaPrimitiveType
//                ).invoke(device, true)
//                Log.d(TAG, "Success to setPairingConfirmation.")
//            } catch (e: Exception) {
//                // TODO Auto-generated catch block
//                Log.e(TAG, e.message.toString())
//                e.printStackTrace()
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, e.message.toString())
//            e.printStackTrace()
//        }
    }
