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
}
