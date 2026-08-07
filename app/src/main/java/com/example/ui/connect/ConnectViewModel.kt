package com.example.ui.connect

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY

val Context.dataStore by preferencesDataStore(name = "novora_prefs")

class ConnectViewModel(private val context: Context) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return ConnectViewModel(application) as T
            }
        }
    }
    private val URL_KEY = stringPreferencesKey("server_url")

    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUrl = context.dataStore.data.map { prefs -> prefs[URL_KEY] ?: "" }.first()
            _url.value = savedUrl
        }
    }

    fun onUrlChange(newUrl: String) {
        _url.value = newUrl
    }

    fun connect() {
        val currentUrl = _url.value
        if (currentUrl.isNotBlank()) {
            viewModelScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[URL_KEY] = currentUrl
                }
                _isConnected.value = true
            }
        }
    }
}
