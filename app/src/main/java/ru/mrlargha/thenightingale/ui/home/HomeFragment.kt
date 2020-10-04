package ru.mrlargha.thenightingale.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.mrlargha.thenightingale.databinding.FragmentHomeBinding
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        const val TAG: String = "HomeFragment"
    }

    @Inject lateinit var viewModel: HomeViewModel

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
        Log.d(TAG, "View created")
        return binding.root
    }

    private fun subscribeUI(binding: FragmentHomeBinding) {
        viewModel.musicFilesLiveData.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as MusicFileAdapter).data = it
            Log.d(TAG, "List changed!")
        }
    }

}