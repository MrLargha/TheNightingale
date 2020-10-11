package ru.mrlargha.thenightingale.ui.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.preference.PreferenceManager
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import ru.mrlargha.thenightingale.NightingaleApp
import ru.mrlargha.thenightingale.tools.Utils

class BluetoothSetupViewModel @ViewModelInject constructor(
    application: Application,
) :
    AndroidViewModel(application) {
    /**
     * MutableLiveData containing the list of devices.
     */
    private var devicesLiveData: DevicesLiveData? = null

    /**
     * MutableLiveData containing the scanner state.
     */
    var scannerStateLiveData: BLEScannerStateLiveData? = null
        private set

    private var preferences: SharedPreferences? = null

    fun getDevices(): DevicesLiveData? {
        return devicesLiveData
    }


    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(application)
        scannerStateLiveData = BLEScannerStateLiveData(
            Utils.isBleEnabled,
            Utils.isLocationEnabled(application)
        )
        devicesLiveData = DevicesLiveData()
        registerBroadcastReceivers(application)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<NightingaleApp>().unregisterReceiver(bluetoothStateBroadcastReceiver)
        if (Utils.isMarshmallowOrAbove) {
            getApplication<Application>().unregisterReceiver(locationProviderChangedReceiver)
        }
    }

    /**
     * Forces the observers to be notified. This method is used to refresh the screen after the
     * location permission has been granted. In result, the observer in
     * [no.nordicsemi.android.blinky.ScannerActivity] will try to start scanning.
     */
    fun refresh() {
        scannerStateLiveData!!.refresh()
    }

    /**
     * Start scanning for Bluetooth devices.
     */
    fun startScan() {
        if (scannerStateLiveData!!.isScanning) {
            return
        }

        // Scanning settings
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.startScan(null, settings, scanCallback)
        scannerStateLiveData!!.scanningStarted()
    }

    /**
     * Stop scanning for bluetooth devices.
     */
    fun stopScan() {
        if (scannerStateLiveData!!.isScanning && scannerStateLiveData!!.isBluetoothEnabled) {
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            scannerStateLiveData!!.scanningStopped()
        }
    }

    // TODO: Fix non null assertion
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // This callback will be called only if the scan report delay is not set or is set to 0.

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired(getApplication()) && !Utils.isLocationEnabled(
                    getApplication()
                )
            ) Utils.markLocationNotRequired(getApplication())
            if (devicesLiveData?.deviceDiscovered(result)!!) {
                scannerStateLiveData!!.recordFound()
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
                devicesLiveData?.deviceDiscovered(result)!! || atLeastOneMatchedFilter
            if (atLeastOneMatchedFilter) {
                scannerStateLiveData!!.recordFound()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // TODO This should be handled
            scannerStateLiveData!!.scanningStopped()
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
            scannerStateLiveData!!.setLocationEnabled(enabled)
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
                BluetoothAdapter.STATE_ON -> scannerStateLiveData!!.bluetoothEnabled()
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                    stopScan()
                    scannerStateLiveData!!.bluetoothDisabled()
                }
            }
        }
    }
}