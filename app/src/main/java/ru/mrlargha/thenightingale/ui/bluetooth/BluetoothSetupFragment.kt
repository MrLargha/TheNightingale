package ru.mrlargha.thenightingale.ui.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.mrlargha.thenightingale.databinding.FragmentBluetoothSetupBinding
import ru.mrlargha.thenightingale.tools.Utils


class BluetoothSetupFragment : Fragment() {

    private val viewModel: BluetoothSetupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBluetoothSetupBinding.inflate(inflater, container, false)



        binding.apply {
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = BLEDeviceAdapter()
            startScanButton.setOnClickListener { startScan() }
        }

        subscribeUI(binding)

        return binding.root
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == no.nordicsemi.android.blinky.ScannerActivity.REQUEST_ACCESS_FINE_LOCATION) {
//            viewModel.refresh()
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.startScan()
                }
            }
        }
    }


    private fun startScan() {
        // Location must be enabled.
        if (!Utils.isLocationPermissionsGranted(requireContext())) {
            val requestLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        // Attempting to scan again
                        startScan()
                    }
                }
            Utils.markLocationPermissionRequested(requireContext())
            requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Bluetooth must be enabled.
        if (viewModel.scannerStateLiveData.value?.isBluetoothEnabled == true) {
            viewModel.startScan()
        } else {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent)
            viewModel.startScan()
        }

//        @Suppress("DEPRECATION")
//        requestPermissions(
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//            REQUEST_ACCESS_FINE_LOCATION
//        )

    }

    private fun subscribeUI(binding: FragmentBluetoothSetupBinding) {
        viewModel.devicesLiveData.observe(viewLifecycleOwner, { devices ->
            (binding.recycler.adapter as BLEDeviceAdapter).devicesList = devices.devices
        })
    }

    companion object {
        private const val REQUEST_ACCESS_FINE_LOCATION: Int = 2281
    }
}