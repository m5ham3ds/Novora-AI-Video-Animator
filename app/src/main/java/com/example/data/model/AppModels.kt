package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ServerConfig(val baseUrl: String, val isValid: Boolean)

data class GenerationRequest(val imageUri: String, val audioUri: String, val selectedModel: String)

data class GenerationResponse(val videoUrl: String, val success: Boolean, val message: String)

@Entity(tableName = "history_records")
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoPath: String,
    val thumbnailPath: String,
    val modelUsed: String,
    val timestamp: Long
)
