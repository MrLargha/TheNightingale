package ru.mrlargha.thenightingale.ui.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.mrlargha.thenightingale.data.models.DiscoveredBLEDevice
import ru.mrlargha.thenightingale.databinding.BleDeviceViewBinding

typealias ClickListener = (device: DiscoveredBLEDevice) -> Unit

class BLEDeviceAdapter : RecyclerView.Adapter<BLEDeviceAdapter.BLEDeviceViewHolder>() {

    var onConnectClickListener: ClickListener? = null

    var devicesList: List<DiscoveredBLEDevice> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class BLEDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            BleDeviceViewBinding.bind(itemView).apply {
                connectButton.setOnClickListener {
                    onConnectClickListener?.invoke(devicesList[adapterPosition])
                }
            }
        }

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