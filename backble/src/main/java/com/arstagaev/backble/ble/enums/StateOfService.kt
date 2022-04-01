package com.arstagaev.backble.ble.enums

enum class StateOfService(s: String) {
    INIT("init"),

    NEUTRAL("neutral"),

    //OFF("off"),
    NO_CONNECTED("no connected"),
    CONNECTING("connecting"),
    DISCONNECTING("disconnecting"),
    LOSS_CONNECTION_AND_WAIT_NEW("loss connection"),
    CONNECTED_BUT_NO_SUBSCRIBED("no recording"),

    NOTIFYING_OR_INDICATING("notify_indicate"),

    UNBONDING("unbonding"),
}