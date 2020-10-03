package ru.mrlargha.thenightingale.data.models

import android.net.Uri

data class MusicFileInfo(
    val name: String,
    val artist: String,
    val size: Int,
    val contentUri: Uri
) {
}