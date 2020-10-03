package ru.mrlargha.thenightingale.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.mrlargha.thenightingale.R
import ru.mrlargha.thenightingale.databinding.FragmentHomeBinding
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var viewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MusicFileAdapter()
        }

        subscribeUI(binding)

        return binding.root
    }

    private fun subscribeUI(binding: FragmentHomeBinding) {
        viewModel.musicFilesLiveData.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as MusicFileAdapter).data = it
        }
    }

}