package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.StorageMonitorRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodScan20Dao {
    @Query("SELECT * FROM freshness_vision_records ORDER BY timestamp DESC")
    fun getAllFreshnessRecords(): Flow<List<FreshnessVisionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreshnessRecord(record: FreshnessVisionRecord): Long

    @Delete
    suspend fun deleteFreshnessRecord(record: FreshnessVisionRecord)

    @Query("SELECT * FROM chemical_ph_records ORDER BY timestamp DESC")
    fun getAllChemicalPhRecords(): Flow<List<ChemicalPhRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicalPhRecord(record: ChemicalPhRecord): Long

    @Delete
    suspend fun deleteChemicalPhRecord(record: ChemicalPhRecord)

    @Query("SELECT * FROM storage_monitor_records ORDER BY timestamp DESC")
    fun getAllStorageRecords(): Flow<List<StorageMonitorRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorageRecord(record: StorageMonitorRecord): Long

    @Delete
    suspend fun deleteStorageRecord(record: StorageMonitorRecord)
}
