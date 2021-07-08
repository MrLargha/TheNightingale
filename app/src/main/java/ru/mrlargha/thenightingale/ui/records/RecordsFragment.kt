package ru.mrlargha.thenightingale.ui.records

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.mrlargha.thenightingale.R
import ru.mrlargha.thenightingale.databinding.FragmentRecordsBinding
import ru.mrlargha.thenightingale.ui.MusicFileAdapter

@AndroidEntryPoint
class RecordsFragment : Fragment() {

    private val viewModel: RecordsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRecordsBinding.inflate(layoutInflater, container, false)
        val adapter = RecordsAdapter()
        binding.recordsRecycler.adapter = adapter
        binding.recordsRecycler.layoutManager = LinearLayoutManager(context)
        viewModel.musicRepository.getRecords().observe(viewLifecycleOwner) { files ->
            adapter.data = files
        }

        return binding.root
    }

}