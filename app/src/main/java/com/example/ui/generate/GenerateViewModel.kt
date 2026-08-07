package com.example.ui.generate

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.repository.NovoraRepository
import com.example.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GenerateState {
    IDLE, LOADING, SUCCESS, ERROR
}

class GenerateViewModel(private val application: Application) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return GenerateViewModel(app) as T
            }
        }
    }

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _audioUri = MutableStateFlow<Uri?>(null)
    val audioUri = _audioUri.asStateFlow()

    private val _selectedModel = MutableStateFlow("EchoMimicV3")
    val selectedModel = _selectedModel.asStateFlow()

    private val _state = MutableStateFlow(GenerateState.IDLE)
    val state = _state.asStateFlow()

    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl = _videoUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun setAudioUri(uri: Uri?) {
        _audioUri.value = uri
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun generateVideo(baseUrl: String) {
        val imgUri = _imageUri.value
        val audUri = _audioUri.value
        if (imgUri == null || audUri == null) {
            _errorMessage.value = "يرجى اختيار صورة وملف صوتي"
            return
        }

        val imageFile = FileUtils.uriToFile(application, imgUri)
        val audioFile = FileUtils.uriToFile(application, audUri)

        if (imageFile == null || audioFile == null) {
            _errorMessage.value = "فشل في قراءة الملفات"
            return
        }

        viewModelScope.launch {
            _state.value = GenerateState.LOADING
            _errorMessage.value = null

            val repository = NovoraRepository(baseUrl)
            val result = repository.generateVideo(imageFile, audioFile, _selectedModel.value)

            if (result.isSuccess) {
                _videoUrl.value = result.getOrNull()
                _state.value = GenerateState.SUCCESS
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                _state.value = GenerateState.ERROR
            }
        }
    }
}
