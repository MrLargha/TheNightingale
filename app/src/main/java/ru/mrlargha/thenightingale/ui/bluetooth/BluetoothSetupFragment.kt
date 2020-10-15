package ru.mrlargha.thenightingale.ui.bluetooth

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.mrlargha.thenightingale.databinding.FragmentBluetoothSetupBinding
import ru.mrlargha.thenightingale.tools.Utils

@AndroidEntryPoint
class BluetoothSetupFragment : Fragment() {

    private val viewModel: BluetoothSetupViewModel by viewModels()
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                // Attempting to scan again
                startScan()
            }
        }

    private val bluetoothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                // TODO: Notify user, that he fucked up
            } else {
                startScan()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBluetoothSetupBinding.inflate(inflater, container, false)

        binding.apply {
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = BLEDeviceAdapter().apply {
                onConnectClickListener = {
                    viewModel.connectToDevice(it)
                }
            }
            startScanButton.setOnClickListener {
                if (viewModel.scannerStateLiveData.value?.isScanning == true)
                    viewModel.stopScan()
                else
                    startScan()
            }
        }

        registerBroadcastReceivers(activity?.application!!)

        subscribeUI(binding)

        return binding.root
    }

    private fun startScan() {
        // Location must be enabled.
        if (!Utils.isLocationPermissionsGranted(requireContext())) {
            Utils.markLocationPermissionRequested(requireContext())
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        // BT must be enabled
        if (viewModel.scannerStateLiveData.value?.isBluetoothEnabled == true) {
            viewModel.startScan()
        } else {
            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun subscribeUI(binding: FragmentBluetoothSetupBinding) {
        viewModel.devicesLiveData.observe(viewLifecycleOwner, { devices ->
            (binding.recycler.adapter as BLEDeviceAdapter).devicesList = devices.devices
        })
        viewModel.scannerStateLiveData.observe(viewLifecycleOwner, {
            binding.apply {
                progressBar.visibility = if (it.isScanning) View.VISIBLE else View.GONE
                startScanButton.text = if (it.isScanning) "Stop scan" else "start scan"
                scannerState.text =
                    if (it.isScanning) "Scanning in progress" else "Scanning stopped"
            }
        })
        viewModel.bondStateLiveData.observe(viewLifecycleOwner, {
            binding.apply {
                if (it.isConnected) {
                    connectionState.text =
                        "Connected to " + (viewModel.bleManager.bluetoothDevice?.name
                            ?: viewModel.bleManager.bluetoothDevice?.address)
                    viewModel.stopScan()
                } else {
                    connectionState.text = "No device connected"
                }
            }
        })
    }

    private fun registerBroadcastReceivers(application: Application) {
        application.registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            val previousState = intent.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                BluetoothAdapter.STATE_OFF
            )
            when (state) {
//                BluetoothAdapter.STATE_ON -> {
//                    startScan()
//                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF ->
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF
                        && previousState != BluetoothAdapter.STATE_OFF
                    ) {
//                        viewModel.scannerStateLiveData.value =
//                            viewModel.scannerStateLiveData.value?.apply {
//                                isScanning = false
//                                isBluetoothEnabled = false
//                            }
                        // TODO: Notify user that he fucked up, and stop scan
                    }
            }
        }
    }
}