package ru.mrlargha.thenightingale.ui.bluetooth

import androidx.lifecycle.MutableLiveData

class BLEScannerStateLiveData(
    var isBluetoothEnabled: Boolean,
    locationEnabled: Boolean
) :
    MutableLiveData<BLEScannerStateLiveData>() {
    var isScanning = false
        private set
    private var hasRecords = false
    private var locationEnabled: Boolean

    init {
        this.locationEnabled = locationEnabled
        postValue(this)
    }

    fun refresh() {
        postValue(this)
    }

    fun scanningStarted() {
        isScanning = true
        postValue(this)
    }

    fun scanningStopped() {
        isScanning = false
        postValue(this)
    }

    fun bluetoothEnabled() {
        isBluetoothEnabled = true
        postValue(this)
    }

    @Synchronized
    fun bluetoothDisabled() {
        isBluetoothEnabled = false
        hasRecords = false
        postValue(this)
    }

    fun setLocationEnabled(enabled: Boolean) {
        locationEnabled = enabled
        postValue(this)
    }

    fun recordFound() {
        hasRecords = true
        postValue(this)
    }

    /**
     * Returns whether any records matching filter criteria has been found.
     */
    fun hasRecords(): Boolean {
        return hasRecords
    }

    /**
     * Returns whether Location is enabled.
     */
    fun isLocationEnabled(): Boolean {
        return locationEnabled
    }

    /**
     * Notifies the observer that scanner has no records to show.
     */
    fun clearRecords() {
        hasRecords = false
        postValue(this)
    }
}
