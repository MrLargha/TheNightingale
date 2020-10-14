package ru.mrlargha.thenightingale.ui.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.preference.PreferenceManager
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import ru.mrlargha.thenightingale.NightingaleApp
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

    init {
        registerBroadcastReceivers(application)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<NightingaleApp>().unregisterReceiver(bluetoothStateBroadcastReceiver)
        if (Utils.isMarshmallowOrAbove) {
            getApplication<Application>().unregisterReceiver(locationProviderChangedReceiver)
        }
    }

    fun startScan(): Unit? =
        scannerStateLiveData.value?.let {
            if (!it.isScanning) {
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(500)
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

        // TODO: Fix non null assertion
        override fun onBatchScanResults(results: List<ScanResult>) {
            // This callback will be called only if the report delay set above is greater then 0.

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired(getApplication()) && !Utils.isLocationEnabled(
                    getApplication()
                )
            ) Utils.markLocationNotRequired(getApplication())
            var atLeastOneMatchedFilter = false
            for (result in results) atLeastOneMatchedFilter =
                devicesLiveData.value?.deviceDiscovered(result) ?: false || atLeastOneMatchedFilter
            if (atLeastOneMatchedFilter) {
                scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                    hasRecords = true
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                isScanning = false
            }
        }
    }

    /**
     * Register for required broadcast receivers.
     */
    private fun registerBroadcastReceivers(application: Application) {
        application.registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        if (Utils.isMarshmallowOrAbove) {
            application.registerReceiver(
                locationProviderChangedReceiver,
                IntentFilter(LocationManager.MODE_CHANGED_ACTION)
            )
        }
    }

    /**
     * Broadcast receiver to monitor the changes in the location provider.
     */
    private val locationProviderChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val enabled: Boolean = Utils.isLocationEnabled(context)
            scannerStateLiveData.value = scannerStateLiveData.value?.apply {
                isLocationEnabled = enabled
            }
        }
    }

    /**
     * Broadcast receiver to monitor the changes in the bluetooth adapter.
     */
    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            val previousState = intent.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                BluetoothAdapter.STATE_OFF
            )
            when (state) {
                BluetoothAdapter.STATE_ON -> scannerStateLiveData.value =
                    scannerStateLiveData.value?.apply {
                        isBluetoothEnabled = false
                    }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF ->
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF
                        && previousState != BluetoothAdapter.STATE_OFF
                    ) {
                        scannerStateLiveData.value =
                            scannerStateLiveData.value?.apply {
                                isScanning = false
                                isBluetoothEnabled = false
                            }
                    }
            }
        }
    }
}