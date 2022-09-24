
package com.arstagaev.flowble.extensions

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.arstagaev.flowble.constants.AllGattDescriptors.ClientCharacteristicConfiguration
import com.arstagaev.flowble.gentelman_kit.logError
import com.arstagaev.flowble.gentelman_kit.logInfo
import java.util.Locale
import java.util.UUID

/** UUID of the Client Characteristic Configuration Descriptor (0x2902). */
const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"

// BluetoothGatt

fun BluetoothGatt.printGattTable() {
    if (services.isEmpty()) {
        Log.i("ccc","No service and characteristic available, call discoverServices() first?")
        return
    }
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { char ->
            var description = "${char.uuid}: ${char.printProperties()}"
            if (char.descriptors.isNotEmpty()) {
                description += "\n" + char.descriptors.joinToString(
                    separator = "\n|------",
                    prefix = "|------"
                ) { descriptor ->
                    "${descriptor.uuid}: ${descriptor.printProperties()}"
                }
            }
            description
        }
        logInfo("Service ${service.uuid}\nCharacteristics:\n$characteristicsTable")
    }
}

fun BluetoothGatt.findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
    services?.forEach { service ->
        service.characteristics?.firstOrNull { characteristic ->
            characteristic.uuid == uuid
        }?.let { matchingCharacteristic ->
            logInfo(" I FOUND CHARACTERISTIC: ${matchingCharacteristic.uuid.toString()}")
            return matchingCharacteristic
        }
    }
    logInfo(" I DONT FOUND CHARACTERISTIC:")
    return null
}

fun BluetoothGatt.findDescriptor(uuid: UUID): BluetoothGattDescriptor? {
    services?.forEach { service ->
        service.characteristics.forEach { characteristic ->
            characteristic.descriptors?.firstOrNull { descriptor ->
                descriptor.uuid == uuid
            }?.let { matchingDescriptor ->
                return matchingDescriptor
            }
        }
    }
    return null
}

@SuppressLint("MissingPermission")
fun BluetoothManager.weHaveDevice(inputAddress: String) : Boolean {
    if (adapter == null) return false

    var listOfDevices = getConnectedDevices(BluetoothProfile.GATT)
    var weHaveDevice = false
    listOfDevices.forEach {
        if (it.address == inputAddress) {
            weHaveDevice = true
        }

    }
    return weHaveDevice
}

// BluetoothGattCharacteristic

fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isIndicatable()) add("INDICATABLE")
    if (isNotifiable()) add("NOTIFIABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

// BluetoothGattDescriptor

fun BluetoothGattDescriptor.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

// is work well, try with on\off notify:
// when enabled I found { 0x01 00 }
// TODO: need check with indicate
fun BluetoothGattDescriptor.isEnabled(): Boolean =
    character(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) || character(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)

// not work, try with on\off notify:
fun BluetoothGattDescriptor.isDisabled(): Boolean =
    character(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0

fun BluetoothGattDescriptor.character(type: ByteArray): Boolean =
    value.contentEquals(type)

/**
 * Convenience extension function that returns true if this [BluetoothGattDescriptor]
 * is a Client Characteristic Configuration Descriptor.
 */
fun BluetoothGattDescriptor.isCccd() =
    uuid.toString().toUpperCase(Locale.US) == ClientCharacteristicConfiguration.toUpperCase(Locale.US)

// ByteArray

fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

fun BluetoothDevice.removeBond() {
    try {
        this.javaClass.getMethod("removeBond").invoke(this)
    }catch ( e: Exception) {

        repeat(10) {
            logError("unbond NOT SUCCESS >> ${e.message} <<")
        }

    }
}
