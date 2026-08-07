package com.example.ui.connect

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.utils.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


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

    var serverUrl by mutableStateOf("")
        private set
    var isConnecting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val savedUrl = context.dataStore.data.map { prefs -> prefs[URL_KEY] ?: "" }.first()
            serverUrl = savedUrl
        }
    }

    fun updateUrl(newUrl: String) {
        serverUrl = newUrl
        errorMessage = null
    }

    fun onConnectClick(navController: NavController) {
        if (serverUrl.isBlank()) return
        
        var finalUrl = serverUrl.trim()
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        
        if (!finalUrl.contains(Regex("""\.gradio\.live"""))) {
            errorMessage = "يجب أن يحتوي الرابط على .gradio.live"
            return
        }

        viewModelScope.launch {
            isConnecting = true
            context.dataStore.edit { prefs ->
                prefs[URL_KEY] = finalUrl
            }
            navController.popBackStack()
            isConnecting = false
        }
    }
}
