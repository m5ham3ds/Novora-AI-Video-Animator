package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.data.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryRecord>>

    @Insert
    suspend fun insertRecord(record: HistoryRecord)

    @Delete
    suspend fun deleteRecord(record: HistoryRecord)
}
