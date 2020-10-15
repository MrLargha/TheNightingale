package ru.mrlargha.thenightingale.ui.bluetooth

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import ru.mrlargha.thenightingale.data.models.BLEScannerState
import ru.mrlargha.thenightingale.data.models.DevicesList
import ru.mrlargha.thenightingale.tools.Utils

class BluetoothSetupViewModel @ViewModelInject constructor(
    application: Application
) :
    AndroidViewModel(application) {

    var devicesLiveData: MutableLiveData<DevicesList> = MutableLiveData(DevicesList())
        private set
    var scannerStateLiveData: MutableLiveData<BLEScannerState> =
        MutableLiveData(BLEScannerState(Utils.isBleEnabled, Utils.isLocationEnabled(application)))
        private set

    fun startScan(): Unit? =
        scannerStateLiveData.value?.let {
            if (!it.isScanning) {
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(5000)
                    .setUseHardwareBatchingIfSupported(false)
                    .build()
                val scanner = BluetoothLeScannerCompat.getScanner()
                scanner.startScan(null, settings, scanCallback)
                scannerStateLiveData.value = it.apply {
                    isScanning = true
                }
            }
        }

    fun stopScan() = scannerStateLiveData.value?.let {
        if (it.isScanning && it.isBluetoothEnabled) {
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            scannerStateLiveData.value = it.apply {
                isScanning = false
            }
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // This callback will be called only if the scan report delay is not set or is set to 0.
            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired(getApplication()) && !Utils.isLocationEnabled(
                    getApplication()
                )
            ) Utils.markLocationNotRequired(getApplication())

            devicesLiveData.value?.let {
                if (it.deviceDiscovered(result)) {
                    scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                        hasRecords = true
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            if (Utils.isLocationRequired(getApplication()) && !Utils.isLocationEnabled(
                    getApplication()
                )
            ) Utils.markLocationNotRequired(getApplication())
            for (result in results)
                devicesLiveData.value?.deviceDiscovered(result)
            scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                hasRecords = true
            }

            // Dirty MVVM hack
            devicesLiveData.value = devicesLiveData.value

        }

        override fun onScanFailed(errorCode: Int) {
            scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                isScanning = false
            }
        }
    }
}