package ru.mrlargha.thenightingale.data.models

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore

class IntensityRecord(
    @Embedded
    val trackInfo: MusicFileInfo,
    val recordedDataUri: Uri
) {
}

data class MusicFileInfo(
    val name: String,
    val contentUri: Uri,
    val duration: Int = 0,
) {
    companion object {
        @Ignore
        private const val THUMB_SIZE = 64
    }

    fun loadThumbnail() =
        ThumbnailUtils.extractThumbnail(
            BitmapFactory.decodeFile(contentUri.encodedPath),
            THUMB_SIZE, THUMB_SIZE
        )
}