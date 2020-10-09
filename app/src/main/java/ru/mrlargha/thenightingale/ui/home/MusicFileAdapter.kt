package ru.mrlargha.thenightingale.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.databinding.MusicFileViewBinding
import ru.mrlargha.thenightingale.ui.record.RecordFragment

class MusicFileAdapter(_data: List<MusicFileInfo> = emptyList()) :
    RecyclerView.Adapter<MusicFileAdapter.MusicFileViewHolder>() {

    var data: List<MusicFileInfo> = _data
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MusicFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(musicFileInfo: MusicFileInfo) {
            val binding = MusicFileViewBinding.bind(itemView)
            binding.apply {
                trackName.text = musicFileInfo.name
                trackAuthor.text = musicFileInfo.artist
                duration.text = musicFileInfo.durationString
                CoroutineScope(Job() + Dispatchers.IO).launch {
                    val bitmap = musicFileInfo.loadThumbnail(itemView.context.applicationContext)
                    launch(Dispatchers.Main) {
                        thumbnail.setImageBitmap(bitmap)
                    }
                }
                recordButton.setOnClickListener {
                    val action =
                        HomeFragmentDirections.actionNavigationHomeToRecordFragment(
                            musicFileInfo.contentUri.toString()
                        )
                    root.findNavController().navigate(action)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MusicFileViewHolder(
        MusicFileViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
    )

    override fun onBindViewHolder(holder: MusicFileViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size
}