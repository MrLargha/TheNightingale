package ru.mrlargha.thenightingale.ui.bluetooth

import androidx.lifecycle.LiveData
import no.nordicsemi.android.support.v18.scanner.ScanResult
import ru.mrlargha.thenightingale.data.models.DiscoveredBLEDevice
import java.util.*

class DevicesLiveData :
    LiveData<List<DiscoveredBLEDevice?>?>() {
    private val devices: MutableList<DiscoveredBLEDevice> =
        ArrayList<DiscoveredBLEDevice>()
    private var filteredDevices: List<DiscoveredBLEDevice>? = null

    fun bluetoothDisabled() {
        devices.clear()
        filteredDevices = null
        postValue(null)
    }

    fun deviceDiscovered(result: ScanResult): Boolean {
        val device: DiscoveredBLEDevice

        // Check if it's a new device.
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

    /**
     * Clears the list of devices.
     */
    fun clear() {
        devices.clear()
        filteredDevices = null
        postValue(null)
    }


    /**
     * Finds the index of existing devices on the device list.
     *
     * @param result scan result.
     * @return Index of -1 if not found.
     */
    private fun indexOf(result: ScanResult): Int {
        for ((i, device) in devices.withIndex()) {
            if (device.matches(result)) return i
        }
        return -1
    }
}
