package ru.mrlargha.thenightingale.data.models

import no.nordicsemi.android.ble.data.Data

class BLEMotor {
    companion object {
        private const val MOTOR_OFF: Byte = 0x00

        fun setSpeed(speed: Byte): Data = Data.opCode(speed)
        fun stopMotor(): Data = Data.opCode(MOTOR_OFF)
    }
}