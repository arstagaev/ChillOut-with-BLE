package com.arstagaev.backble.ble.enums

enum class Actions {
    START,              // global goal: start service
    STOP,               // global goal: stop  service, and after that - start parsing
    FORCE_STOP,         // when we force exit from app, and kill him, if we do not stop services - services will may not killing himselfs

    UNBOND,             // aim is unbond current device

    NEUTRAL_CONNECTED,  // aim is stop rec
    SUBS_AND_CONNECTED, // aim is make rec again
    SCAN_START,         // aim is start scan
    SCAN_STOP,          // aim is stop scan
    TARGET_CONNECT,     // need for first bonding of ble tag fixme imho need delete it
    MISC
}
