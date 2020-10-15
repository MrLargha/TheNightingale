package ru.mrlargha.thenightingale.data.models

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.support.v18.scanner.ScanResult

class DiscoveredBLEDevice(scanResult: ScanResult) {
    val bluetoothDevice: BluetoothDevice = scanResult.device
    private var lastScanResult: ScanResult? = null
    private var previousRssi = 0

    val address: String = bluetoothDevice.address

    var name: String? = null
        private set

    var rssi = 0
        private set

    var highestRssi = -128
        private set

    init {
        update(scanResult)
    }

    /**
     * Updates the device values based on the scan result.
     *
     * @param scanResult the new received scan result.
     */
    fun update(scanResult: ScanResult) {
        lastScanResult = scanResult
        name = if (scanResult.scanRecord != null) scanResult.scanRecord!!.deviceName else null
        previousRssi = rssi
        rssi = scanResult.rssi
        if (highestRssi < rssi) highestRssi = rssi
    }

    fun matches(scanResult: ScanResult): Boolean {
        return bluetoothDevice.address == scanResult.device.address
    }
}
