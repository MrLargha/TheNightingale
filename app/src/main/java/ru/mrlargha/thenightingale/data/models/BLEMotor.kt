package ru.mrlargha.thenightingale.data.models

import no.nordicsemi.android.ble.data.Data

class BLEMotor {
    companion object {
        private const val MOTOR_OFF: Byte = 0x00

        fun setSpeed(speed: Int): Data = Data(speed.toByteArray())
        fun stopMotor(): Data = Data.opCode(MOTOR_OFF)

        private fun Int.toByteArray(): ByteArray {
            return byteArrayOf(
                (this shr 24 and 0xff).toByte(),
                (this shr 16 and 0xff).toByte(),
                (this shr 8 and 0xff).toByte(),
                (this shr 0 and 0xff).toByte()
            ).reversed().toByteArray()
        }

    }


}