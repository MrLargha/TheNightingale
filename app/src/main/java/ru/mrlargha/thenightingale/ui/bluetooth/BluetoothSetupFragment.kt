package ru.mrlargha.thenightingale.ui.bluetooth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.mrlargha.thenightingale.databinding.FragmentBluetoothSetupBinding


class BluetoothSetupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBluetoothSetupBinding.inflate(inflater, container, false)

        return binding.root
    }
}