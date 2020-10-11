package ru.mrlargha.thenightingale.ui.recording

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

    private var chartValues: List<Entry> = emptyList()
    private lateinit var binding: FragmentRecordBinding
    private var chartUpdateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordBinding.inflate(inflater, container, false)

        binding.apply {
            recordButton.setOnClickListener {
                viewModel.invertStatus()
            }
            slider2.addOnChangeListener { _, value, _ ->
                viewModel.currentIntensity.set(value.toInt())
            }
            slider2.valueFrom = 0f
            slider2.valueTo = 255f
            chart.axisLeft.apply {
                setDrawAxisLine(false)
                axisMaximum = 256f
                setDrawGridLines(false)
            }
            chart.axisRight.apply {
                setDrawAxisLine(false)
                axisMaximum = 256f
                setDrawGridLines(false)
            }
            chart.xAxis.apply {
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            chart.legend.isEnabled = false
            chart.isClickable = false
            chart.isEnabled = false
        }

        viewModel.fileUri = Uri.parse(navArgs.musicFileUri)
        subscribeUI()

        return binding.root
    }

    private fun updateChart() {
        binding.chart.data = LineData(LineDataSet(chartValues, "").apply {
            setDrawCircles(false)
        })
        binding.chart.xAxis.mAxisMaximum = chartValues.last().x
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
                            chartUpdateJob = lifecycleScope.launch {
                                while (true) {
                                    chartValues = viewModel.recordData.takeLast(100)
                                        .map { Entry(it.first.toFloat(), it.second.toFloat()) }
                                    launch(Dispatchers.Main) {
                                        binding.chart.xAxis.mAxisMaximum = chartValues.last().x
                                        updateChart()
                                    }
                                    delay(50)
                                }
                            }
                        }

                RecordViewModel.RecordState.STOPPED ->
                    binding.recordButton.setIconResource(R.drawable.record)
                        .also { binding.recordButton.text = "НАЧАТЬ ЗАПИСЬ" }.also {
                            chartUpdateJob?.cancel()
                            chartValues = emptyList()
                        }
            }

            viewModel.maxPosLiveData.observe(viewLifecycleOwner) {
                binding.progress.valueTo = it.toFloat()
            }
        }
    }
}