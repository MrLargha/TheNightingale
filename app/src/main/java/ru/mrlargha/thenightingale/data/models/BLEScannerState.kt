package ru.mrlargha.thenightingale.data.models

data class BLEScannerState(
    var isBluetoothEnabled: Boolean,
    var isLocationEnabled: Boolean,
    var isScanning: Boolean = false,
    var hasRecords: Boolean = false
) {

}