package ru.mrlargha.thenightingale.data.repos

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.mrlargha.thenightingale.data.database.IntensityRecordDao
import ru.mrlargha.thenightingale.data.database.MusicDatabase
import ru.mrlargha.thenightingale.tools.MusicResolver
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val musicResolver: MusicResolver,
    @ApplicationContext context: Context
) {
    private val intensityRecordDao: IntensityRecordDao = MusicDatabase.getInstance(context).intensityRecordDao()

    fun getMusic() = musicResolver.resolveByContentResolver()
    fun getRecords() = intensityRecordDao.getRecords()
}