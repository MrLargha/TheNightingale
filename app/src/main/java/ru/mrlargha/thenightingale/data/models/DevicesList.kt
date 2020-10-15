package ru.mrlargha.thenightingale.data.models

import no.nordicsemi.android.support.v18.scanner.ScanResult

class DevicesList {
    val devices: MutableList<DiscoveredBLEDevice> = mutableListOf()

    fun deviceDiscovered(result: ScanResult): Boolean {
        val device: DiscoveredBLEDevice
        val index = indexOf(result)

        if (index == -1) {
            device = DiscoveredBLEDevice(result)
            devices.add(device)
        } else {
            device = devices[index]
        }

        device.update(result)
        return devices.size > 0
    }

    fun clear() = devices.clear()

    private fun indexOf(result: ScanResult): Int {
        for ((i, device) in devices.withIndex()) {
            if (device.matches(result)) return i
        }
        return -1
    }
}