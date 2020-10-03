package ru.mrlargha.thenightingale.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import ru.mrlargha.thenightingale.databinding.MusicFileViewBinding

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
                trackAuthor.text = "UNKNOWN"
                thumbnail.setImageBitmap(musicFileInfo.loadThumbnail())
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