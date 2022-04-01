package com.arstagaev.backble.ble

import com.arstagaev.chilloutble.ble.BLEParameters.CCC_DESCRIPTOR_UUID
import com.arstagaev.chilloutble.ble.BLEParameters.CHARACTERISTIC_UUID_1
import com.arstagaev.chilloutble.ble.BLEParameters.SCAN_PERIOD
import com.arstagaev.chilloutble.ble.BLEParameters.TRACKING_ADVERTISING
import com.arstagaev.chilloutble.ble.BLEParameters.WITH_NOTIFY


class BLEConfigurator private constructor(builder: Builder) {

    /**
     * Connect by default
     */
    class Builder(val scanperiod: Long, var withNotify : Boolean,var trackingAdvertising : Boolean) {
//        var scanperiod = 10000L
//        var phone: String? = null
//        var address: String? = null
//
        fun setTargetCharacteristicAndDescriptor(crh : String, dscrpt : String): Builder {
            CHARACTERISTIC_UUID_1 = crh
            CCC_DESCRIPTOR_UUID = dscrpt
            return this
        }
//
//        fun phone(phone: String?): Builder {
//            this.phone = phone
//            return this
//        }
//
//        fun address(address: String?): Builder {
//            this.address = address
//            return this
//        }

        //Return the finally consrcuted User object
        fun build(): BLEConfigurator {
            val user = BLEConfigurator(this)
            validate(user)
            return user
        }

        private fun validate(user: BLEConfigurator) {
            //Do some basic validations to check
            //if user object does not break any assumption of system
        }
    }

    init {
        SCAN_PERIOD = builder.scanperiod
        WITH_NOTIFY = builder.withNotify
        TRACKING_ADVERTISING = builder.trackingAdvertising
//        CHARACTERISTIC_UUID= builder.
//        lastName  = builder.lastName
//        phone     = builder.phone
//        address   = builder.address
    }
}