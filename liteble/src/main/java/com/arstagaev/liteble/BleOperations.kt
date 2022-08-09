package com.arstagaev.liteble

enum class BleOperations(var macAddress: String) {
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
}