package com.arstagaev.backble.ble.enums

enum class ConnectingStyle(s: String) {
    MANUAL("manual"),                // connect by manual set
    AUTO_BY_BOND("auto_bond"),       // connect by nearby bond device
    AUTO_BY_SEARCH("auto_search")    // connect by nearby device (device must be set in advance in special list of devices)
}