package com.example.data.repository

import com.example.data.db.HistoryDao
import com.example.data.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryRecord>> = historyDao.getAllHistory()

    suspend fun insert(record: HistoryRecord) {
        historyDao.insertRecord(record)
    }

    suspend fun delete(record: HistoryRecord) {
        historyDao.deleteRecord(record)
    }
}
