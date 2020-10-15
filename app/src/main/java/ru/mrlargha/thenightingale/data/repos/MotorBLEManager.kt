package ru.mrlargha.thenightingale.data.repos

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.Logger
import ru.mrlargha.thenightingale.data.models.BLEMotor
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MotorBLEManager @Inject constructor(@ApplicationContext context: Context) :
    ObservableBleManager(context) {

    companion object {
        private val NG_BLE_MOTOR_UUID_SERVICE =
            UUID.fromString("5cda1af8-66d7-4c32-b628-77c76bfc3cc5")
        private val NG_BLE_MOTOR_UUID_CHARACTERISTIC =
            UUID.fromString("51f24299-22f0-4f22-8bfe-877531f19ff4")
    }

    var logSession: LogSession? = null

    private var motorCharacteristic: BluetoothGattCharacteristic? = null

    protected inner class MotorBLEManagerGattCallback : BleManagerGattCallback() {
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service =
                gatt.getService(NG_BLE_MOTOR_UUID_SERVICE)
            if (service != null) {
                motorCharacteristic =
                    service.getCharacteristic(NG_BLE_MOTOR_UUID_CHARACTERISTIC)
            }
            val rxProperties: Int = motorCharacteristic?.properties ?: 0
            return rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
        }

        override fun onDeviceDisconnected() {
            motorCharacteristic = null
        }
    }

    override fun log(priority: Int, message: String) {
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message)
    }

    override fun shouldClearCacheWhenDisconnected() = true

    override fun getGattCallback() = MotorBLEManagerGattCallback()

    fun setMotorSpeed(speed: Byte) {
        log(Log.VERBOSE, "Mot speed set to $speed}")
        writeCharacteristic(motorCharacteristic, BLEMotor.setSpeed(speed)).enqueue()
    }
}