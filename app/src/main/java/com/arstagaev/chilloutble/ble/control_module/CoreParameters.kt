package com.arstagaev.backble.core

import com.arstagaev.backble.ble.enums.ConnectingStyle

object CoreParameters {
    const val COMMON_FOLDER = "Avtelma"
    var SESSION_NAME_TIME_xyz =   "-"
    var SESSION_NAME_TIME_raw =   "-"

    var NAME_OF_FOLDER_LOGS = "$COMMON_FOLDER/RawData"

    var STARTING_DELAY = 100L
    ///

    var CAR_LICENSE_SIGN_TAG_ADDRESS = "LICENSESIG_000000000000"
    var is_ENABLE_REALTIME_CHART      : Boolean?         = null
    var is_SCORING                    : Boolean?         = null
    var is_ENABLE_DELETE_GARBAGE_LOGS : Boolean?         = null
    var RECORD_ACTIVITY               : Class<*>?        = null
    var RECORD_ACTIVITY_FOR_RAWPARSER : Class<*>?        = null

    var TIME_OF_TRIP = -1 // -1, I set, for check if we get first byte , like start of trip
    /**
     *  CHARTS
     */
    var TRINITY_FOR_CHART = FourthlyDataContainerForChartsXYZ2("~",0f,0f,0f)
}