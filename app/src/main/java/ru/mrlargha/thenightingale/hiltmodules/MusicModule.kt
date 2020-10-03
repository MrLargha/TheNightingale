package ru.mrlargha.thenightingale.hiltmodules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.mrlargha.thenightingale.data.database.IntensityRecordDao
import ru.mrlargha.thenightingale.data.database.MusicDatabase

@Module
@InstallIn(ApplicationComponent::class)
abstract class MusicModule {
//    @Provides
//    fun provideIntensityRecordDao(@ApplicationContext context: Context) : IntensityRecordDao =
//        MusicDatabase.getInstance(context).intensityRecordDao()
}