package com.arstagaev.flowble.gentelman_kit

import com.arstagaev.flowble.constants.AllGattDescriptors
import java.util.UUID

/**
 * This method has been created, because `UUID.fromString(strUUID)` not handle wrong string of input String
 * for this method
 */
fun getUUID(strUUID: String): UUID? {
    if (strUUID.isEmpty() || strUUID.isBlank() || !strUUID.startsWith("0",ignoreCase = true)) {
        logError("WRONG UUID: ${strUUID}, Please change him !")

        return null
    }
    return UUID.fromString(strUUID)
}