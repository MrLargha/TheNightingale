package ru.mrlargha.thenightingale.data.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri

//class IntensityRecord(
//    @Embedded
//    val trackInfo: MusicFileInfo,
//    val recordedDataUri: Uri
//) {
//}

data class MusicFileInfo(
    val name: String,
    val contentUri: Uri,
    val artist: String,
    val duration: Long = 0
) {
    val durationString = "${duration / 60000}:${duration % 60000 / 1000}"

    fun loadThumbnail(context: Context): Bitmap? {
        MediaMetadataRetriever().apply {
            setDataSource(context, contentUri)
            embeddedPicture?.let {
                return BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options())
            }
        }
        return null
    }
    companion object {
        fun createByUri(uri: Uri, context: Context): MusicFileInfo {
            if(uri == Uri.EMPTY)
                return MusicFileInfo("UNKNOWN", uri, "UNKNOWN", 0)
            MediaMetadataRetriever().apply {
                setDataSource(context, uri)
                val artist =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "UNKNOWN"

                val duration =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0

                val title = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: uri.toString()

                return MusicFileInfo(title, uri, artist, duration)
            }
        }
    }
}
