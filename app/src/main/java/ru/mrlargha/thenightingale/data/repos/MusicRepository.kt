package ru.mrlargha.thenightingale.data.repos

import ru.mrlargha.thenightingale.tools.MusicResolver
import javax.inject.Inject

class MusicRepository @Inject constructor(private val musicResolver: MusicResolver) {
    fun getMusic() = musicResolver.resolveByContentResolver()
}