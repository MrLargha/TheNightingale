package ru.mrlargha.thenightingale.ui.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.mrlargha.thenightingale.data.models.DiscoveredBLEDevice
import ru.mrlargha.thenightingale.databinding.BleDeviceViewBinding

class BLEDeviceAdapter : RecyclerView.Adapter<BLEDeviceAdapter.BLEDeviceViewHolder>() {

    var devicesList: List<DiscoveredBLEDevice> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class BLEDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(device: DiscoveredBLEDevice) {
            BleDeviceViewBinding.bind(itemView).apply {
                deviceName.text = device.name ?: device.address
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BLEDeviceViewHolder(
            BleDeviceViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )

    override fun onBindViewHolder(holder: BLEDeviceViewHolder, position: Int) {
        holder.bind(devicesList[position])
    }

    override fun getItemCount(): Int = devicesList.size
}