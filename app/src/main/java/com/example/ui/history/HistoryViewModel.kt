package com.example.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.db.HistoryDatabase
import com.example.data.model.HistoryRecord
import com.example.data.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return HistoryViewModel(app) as T
            }
        }
    }

    private val historyRepository: HistoryRepository
    val historyList: StateFlow<List<HistoryRecord>>

    init {
        val historyDao = HistoryDatabase.getDatabase(application).historyDao()
        historyRepository = HistoryRepository(historyDao)

        historyList = historyRepository.allHistory
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun deleteRecord(record: HistoryRecord) {
        viewModelScope.launch {
            historyRepository.delete(record)
        }
    }
}
