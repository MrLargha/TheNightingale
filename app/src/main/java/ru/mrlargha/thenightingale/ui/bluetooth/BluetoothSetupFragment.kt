package ru.mrlargha.thenightingale.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.mrlargha.thenightingale.databinding.FragmentBluetoothSetupBinding
import ru.mrlargha.thenightingale.services.BLEPlayerService
import ru.mrlargha.thenightingale.services.BLEPlayerService.BLEPlayerServiceBinder


class BluetoothSetupFragment : Fragment() {

    private var mBound: Boolean = false
    private lateinit var mService: BLEPlayerService

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder: BLEPlayerServiceBinder = service as BLEPlayerServiceBinder
            mService = binder.getService()
            mBound = true

            if (!mService.initialize()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 2)
            } else {
                scanLeDevice()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private var mScanning = false
    private val handler = Handler(Looper.myLooper()!!)
    private val SCAN_PERIOD: Long = 100000

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }
    }

    private fun scanLeDevice() {
        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                mScanning = false
                mService.mBluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            mScanning = true
            mService.mBluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            mScanning = false
            mService.mBluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBluetoothSetupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(context, BLEPlayerService::class.java)
        activity?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}