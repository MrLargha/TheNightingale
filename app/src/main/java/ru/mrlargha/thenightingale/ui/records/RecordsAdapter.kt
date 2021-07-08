package ru.mrlargha.thenightingale.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ru.mrlargha.thenightingale.data.models.IntensityRecord
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.databinding.MusicFileViewBinding
import ru.mrlargha.thenightingale.ui.home.HomeFragmentDirections

class RecordsAdapter(_data: List<IntensityRecord> = emptyList()) :
    RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    var data: List<IntensityRecord> = _data
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(musicFileInfo: IntensityRecord) {
            val binding = MusicFileViewBinding.bind(itemView)
            binding.apply {
                trackName.text = musicFileInfo.trackInfo.name
                trackAuthor.text = musicFileInfo.trackInfo.artist
                duration.text = musicFileInfo.trackInfo.durationString
                CoroutineScope(Job() + Dispatchers.IO).launch {
                    val bitmap =
                        musicFileInfo.trackInfo.loadThumbnail(itemView.context.applicationContext)
                    launch(Dispatchers.Main) {
                        thumbnail.setImageBitmap(bitmap)
                    }
                }

                recordButton.setOnClickListener {
                    val action =
                        RecordsFragmentDirections.actionNavigationRecordsToRecordFragment(
                            musicFileInfo.trackInfo.contentUri,
                                    musicFileInfo.id
                        )
                    root.findNavController().navigate(action)
                }
                recordButton.text = "Запустить!"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecordViewHolder(
        MusicFileViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
    )

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size
}