package ru.mrlargha.thenightingale.ui.record

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import ru.mrlargha.thenightingale.R
import ru.mrlargha.thenightingale.databinding.FragmentRecordBinding


@AndroidEntryPoint
class RecordFragment : Fragment() {

    private val viewModel: RecordViewModel by viewModels()
    private val navArgs: RecordFragmentArgs by navArgs()
    private val chartValues: MutableList<Entry> = mutableListOf()
    private lateinit var binding: FragmentRecordBinding
    private var collectDataJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordBinding.inflate(inflater, container, false)

        binding.apply {
            recordButton.setOnClickListener {
                viewModel.invertStatus()
            }
        }

        viewModel.fileUri = Uri.parse(navArgs.musicFileUri)
        subscribeUI()

        return binding.root
    }

    private fun updateChart() {
        binding.chart.data = LineData(LineDataSet(chartValues.takeLast(100), "Trolololol"))
//        binding.chart.xAxis.mAxisMinimum = if(chartValues.last().x - 1000 >= 0) chartValues.last().x - 1000 else 0f
        binding.chart.invalidate()
    }

    private fun subscribeUI() {
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
            binding.progress.value = if (it <= binding.progress.valueTo) it.toFloat() else 0f
        }

        viewModel.playerStatusLiveData.observe(viewLifecycleOwner) { recordState ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (recordState) {
                RecordViewModel.RecordState.RECORDING ->
                    binding.recordButton.setIconResource(R.drawable.stop)
                        .also { binding.recordButton.text = "ОСТАНОВИТЬ ЗАПИСЬ" }.also {
                            collectDataJob = lifecycleScope.launch(Dispatchers.IO) {
                                var i = 0
                                while (true){
                                    i += 10
                                    launch(Dispatchers.Main) {
                                        chartValues.add(Entry(i.toFloat(), binding.slider2.value))
                                        updateChart()
                                    }
                                    delay(50)
                                }
                            }
                        }
                RecordViewModel.RecordState.STOPPED ->
                    binding.recordButton.setIconResource(R.drawable.record)
                        .also { binding.recordButton.text = "НАЧАТЬ ЗАПИСЬ" }.also {
                            collectDataJob?.cancel()
                            chartValues.clear()
                        }
            }
        }

        viewModel.maxPosLiveData.observe(viewLifecycleOwner) {
            binding.progress.valueTo = it.toFloat()
        }
    }
}