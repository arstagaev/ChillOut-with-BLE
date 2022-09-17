package com.arstagaev.flowble.enums

import java.util.*

enum class BleOperations_2(
    var macAddress: String? = null,
    var uuid: UUID? = null,
    var duration: Long? = null,
    var isEnable: Boolean? = null,
    var byteArray: ByteArray? = null
) {
    START_SCAN(""),
    STOP_SCAN(""),
    ADVERTISE(""),

    CONNECT(""),
    DISCONNECT(""),

    DISCOVERY_SERVICES(""),

    WRITE_TO_CHARACTERISTIC(""),
    READ_FROM_CHARACTERISTIC(""),

    ENABLE_NOTIFY_INDICATE(""),
    DISABLE_NOTIFY_INDICATE(""),

    GET_BATTERY_LEVEL(""),

    DELAY(""),
    FULL_STOP(),
}