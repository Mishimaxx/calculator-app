package com.example.test2.data.repository

import com.example.test2.data.database.CalculationDao
import com.example.test2.data.model.CalculationEntry
import com.example.test2.data.model.CalculationType
import kotlinx.coroutines.flow.Flow

/**
 * 計算履歴のリポジトリ
 */
class CalculationRepository(private val calculationDao: CalculationDao) {

    fun getAllEntries(): Flow<List<CalculationEntry>> = calculationDao.getAllEntries()

    fun getEntriesByType(type: CalculationType): Flow<List<CalculationEntry>> = 
        calculationDao.getEntriesByType(type)

    fun getRecentEntries(limit: Int = 25): Flow<List<CalculationEntry>> = 
        calculationDao.getRecentEntries(limit)

    suspend fun insertEntry(entry: CalculationEntry) {
        calculationDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: CalculationEntry) {
        calculationDao.deleteEntry(entry)
    }

    suspend fun deleteAllEntries() {
        calculationDao.deleteAllEntries()
    }

    suspend fun getEntryCount(): Int = calculationDao.getEntryCount()
}
