package com.example.test2.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.test2.data.model.CalculationEntry
import com.example.test2.data.model.CalculationType
import kotlinx.coroutines.flow.Flow

/**
 * 計算履歴のDAO
 */
@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<CalculationEntry>>

    @Query("SELECT * FROM calculation_entries WHERE calculationType = :type ORDER BY timestamp DESC")
    fun getEntriesByType(type: CalculationType): Flow<List<CalculationEntry>>

    @Query("SELECT * FROM calculation_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 25): Flow<List<CalculationEntry>>

    @Insert
    suspend fun insertEntry(entry: CalculationEntry)

    @Delete
    suspend fun deleteEntry(entry: CalculationEntry)

    @Query("DELETE FROM calculation_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT COUNT(*) FROM calculation_entries")
    suspend fun getEntryCount(): Int
}
