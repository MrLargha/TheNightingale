package ru.mrlargha.thenightingale.data.repos

import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class BLEPlayer(@Inject private val bleManager: MotorBLEManager)