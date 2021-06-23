package ru.mrlargha.thenightingale.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.mrlargha.thenightingale.data.models.IntensityRecord

@Database(
    entities = [IntensityRecord::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun intensityRecordDao(): IntensityRecordDao

    companion object {
        @Volatile
        private var instance: MusicDatabase? = null
        private const val DB_NAME = "Nightingale_MUSIC_DB"

        fun getInstance(context: Context): MusicDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): MusicDatabase {
            return Room.databaseBuilder(context, MusicDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration().build()
        }
    }

}