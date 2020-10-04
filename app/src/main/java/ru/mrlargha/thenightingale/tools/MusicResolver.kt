package ru.mrlargha.thenightingale.tools

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.mrlargha.thenightingale.data.models.MusicFileInfo
import javax.inject.Inject


class MusicResolver @Inject constructor(@ApplicationContext private val context: Context) {
    fun resolveByContentResolver(): List<MusicFileInfo> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
        )

        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, null
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

            val musicFiles = mutableListOf<MusicFileInfo>()

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                musicFiles.add(MusicFileInfo.createByUri(contentUri, context))
            }
            return musicFiles
        } ?: return emptyList()
    }
}