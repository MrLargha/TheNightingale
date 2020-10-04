package ru.mrlargha.thenightingale.ui.record

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_record.view.*
import ru.mrlargha.thenightingale.R
import ru.mrlargha.thenightingale.databinding.FragmentRecordBinding

@AndroidEntryPoint
class RecordFragment : Fragment() {

    private val viewModel: RecordViewModel by navGraphViewModels(R.navigation.mobile_navigation)
    private val navArgs: RecordFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRecordBinding.inflate(inflater, container, false)

        binding.apply {
            recordButton.setOnClickListener {
                viewModel.invertStatus()
            }
        }

        view?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                viewModel.stop()
            }
            false;
        }

        viewModel.fileUri = Uri.parse(navArgs.musicFileUri)
        subscribeUI(binding)

        return binding.root
    }

    private fun subscribeUI(binding: FragmentRecordBinding) {
        viewModel.trackInfoLiveData.observe(viewLifecycleOwner) {
            binding.apply {
                artist.text = it.artist
                maxDuration.text = it.durationString
                trackName.text = it.name
                thumbnail.setImageBitmap(it.loadThumbnail(requireContext()))
            }
        }

        viewModel.playerReadyLiveData.observe(viewLifecycleOwner) {
            binding.recordButton.isEnabled = it
        }

        viewModel.currentProgressLiveData.observe(viewLifecycleOwner) {
            binding.currentPosText.text = "${it / 60000}:${it % 60000 / 1000}"
            binding.progress.value = it.toFloat()
        }

        viewModel.playerStatusLiveData.observe(viewLifecycleOwner) { recordState ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (recordState) {
                RecordViewModel.RecordState.RECORDING ->
                    binding.recordButton.setIconResource(R.drawable.stop)
                        .also { binding.recordButton.text = "ОСТАНОВИТЬ ЗАПИСЬ" }
                RecordViewModel.RecordState.STOPPED ->
                    binding.recordButton.setIconResource(R.drawable.record)
                        .also { binding.recordButton.text = "НАЧАТЬ ЗАПИСЬ" }
            }
        }

        viewModel.maxPosLiveData.observe(viewLifecycleOwner) {
            binding.progress.valueTo = it.toFloat()
        }
    }

}