package ru.mrlargha.thenightingale.data.repos

import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class BLEPlayer @Inject constructor(private val bleManager: MotorBLEManager)