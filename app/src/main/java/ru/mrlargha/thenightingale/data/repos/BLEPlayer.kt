package ru.mrlargha.thenightingale.data.repos

import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

class BLEPlayer @Inject constructor(private val bleManager: MotorBLEManager) {
    fun setSpeed(speed: Int) {
        bleManager.setMotorSpeed(speed)
    }
}