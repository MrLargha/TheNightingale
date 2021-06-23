package ru.mrlargha.thenightingale.data.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@Entity(tableName = "records")
class IntensityRecord(
    @Embedded
    val trackInfo: MusicFileInfo,
    val recordedDataUri: String
) {
    @Ignore var data: List<Pair<Int, Int>> = emptyList()

    @PrimaryKey
    var id: Int = -1

    fun loadData() {
        val stream = ObjectInputStream(File(recordedDataUri).inputStream())
        data = stream.readObject() as List<Pair<Int, Int>>
    }

    fun saveData() {
        val stream = ObjectOutputStream(File(recordedDataUri).outputStream())
        stream.writeObject(data)
    }
}

data class MusicFileInfo(
    val name: String,
    val contentUri: String,
    val artist: String,
    val duration: Long
) {
    var durationString = "${duration / 60000}:${duration % 60000 / 1000}"

    fun loadThumbnail(context: Context): Bitmap? {
        MediaMetadataRetriever().apply {
            setDataSource(context, Uri.parse(contentUri))
            embeddedPicture?.let {
                return BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options())
            }
        }
        return null
    }
    companion object {
        fun createByUri(uri: Uri, context: Context): MusicFileInfo {
            if(uri == Uri.EMPTY)
                return MusicFileInfo("UNKNOWN", uri.toString(), "UNKNOWN", 0)
            MediaMetadataRetriever().apply {
                setDataSource(context, uri)
                val artist =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "UNKNOWN"

                val duration =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0

                val title = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: uri.toString()

                return MusicFileInfo(title, uri.toString(), artist, duration)
            }
        }
    }
}
