package ru.mrlargha.thenightingale.ui.bluetooth

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import ru.mrlargha.thenightingale.data.models.BLEScannerState
import ru.mrlargha.thenightingale.data.models.DevicesList
import ru.mrlargha.thenightingale.data.models.DiscoveredBLEDevice
import ru.mrlargha.thenightingale.data.repos.MotorBLEManager
import ru.mrlargha.thenightingale.tools.Utils
import ru.mrlargha.thenightingale.ui.home.HomeFragment.Companion.TAG
import javax.inject.Inject

@HiltViewModel
class BluetoothSetupViewModel @Inject constructor(
    val bleManager: MotorBLEManager,
    @ApplicationContext context: Context
) :
    ViewModel() {

    val devicesLiveData: MutableLiveData<DevicesList> = MutableLiveData(DevicesList())
    val scannerStateLiveData: MutableLiveData<BLEScannerState> =
        MutableLiveData(BLEScannerState(Utils.isBleEnabled, Utils.isLocationEnabled(context)))
    val bondStateLiveData = bleManager.state

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

    fun connectToDevice(device: DiscoveredBLEDevice) {
        Log.d(TAG, "Connecting to ${device.name}")
        bleManager.connect(device.bluetoothDevice).retry(10, 100)
            .useAutoConnect(false)
            .enqueue()
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // This callback will be called only if the scan report delay is not set or is set to 0.
            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired(context) && !Utils.isLocationEnabled(
                    context
                )
            ) Utils.markLocationNotRequired(context)

            devicesLiveData.value?.let {
                if (it.deviceDiscovered(result)) {
                    scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                        hasRecords = true
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            if (Utils.isLocationRequired(context) && !Utils.isLocationEnabled(
                    context
                )
            ) Utils.markLocationNotRequired(context)
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