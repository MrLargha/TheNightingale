package ru.mrlargha.thenightingale.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.mrlargha.thenightingale.data.models.IntensityRecord

@Dao
interface IntensityRecordDao {
    @Insert
    fun insertRecord(record: IntensityRecord)

    @Query("SELECT * FROM records")
    fun getRecords(): LiveData<List<IntensityRecord>>

    @Query("SELECT * FROM records")
    fun getRecordsSync(): List<IntensityRecord>
}