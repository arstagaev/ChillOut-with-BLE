package com.arstagaev.flowble.models

import com.arstagaev.flowble.enums.WriteToCharacteristic
import java.util.UUID

data class CharacterCarrier(
    val uuidCharacteristic: UUID? = null,
    val value: ByteArray? = null,
    val codeStatus: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacterCarrier

        if (uuidCharacteristic != other.uuidCharacteristic) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuidCharacteristic.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
