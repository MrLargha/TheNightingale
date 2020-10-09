package ru.mrlargha.thenightingale.data.repos

import ru.mrlargha.thenightingale.data.database.IntensityRecordDao
import ru.mrlargha.thenightingale.tools.MusicResolver
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val musicResolver: MusicResolver
//    private val intensityRecordDao: IntensityRecordDao
) {
    fun getMusic() = musicResolver.resolveByContentResolver()
}