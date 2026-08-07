package com.example.ui.generate

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.db.HistoryDatabase
import com.example.data.model.HistoryRecord
import com.example.data.repository.HistoryRepository
import com.example.data.repository.NovoraRepository
import com.example.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val historyRepository: HistoryRepository
    init {
        val historyDao = HistoryDatabase.getDatabase(application).historyDao()
        historyRepository = HistoryRepository(historyDao)
    }

    var selectedModel by mutableStateOf("SadTalker")
        private set
    var imageUri by mutableStateOf<Uri?>(null)
        private set
    var audioUri by mutableStateOf<Uri?>(null)
        private set
    var audioFileName by mutableStateOf<String?>(null)
        private set
    var videoUrl by mutableStateOf<String?>(null)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("جاهز للتوليد")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun selectModel(model: String) {
        selectedModel = model
    }

    fun selectImage(uri: Uri) {
        imageUri = uri
        errorMessage = null
    }

    fun selectAudio(uri: Uri, fileName: String) {
        audioUri = uri
        audioFileName = fileName
        errorMessage = null
    }

    fun clearVideo() {
        videoUrl = null
        statusMessage = "جاهز للتوليد"
    }

    fun generateVideo(serverUrl: String, context: Context) {
        if (imageUri == null || audioUri == null) {
            errorMessage = "يرجى اختيار صورة وملف صوتي"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isGenerating = true
            errorMessage = null
            statusMessage = "جارٍ قراءة الملفات..."

            val imageFile = FileUtils.uriToFile(context, imageUri!!)
            val audioFile = FileUtils.uriToFile(context, audioUri!!)

            if (imageFile == null || audioFile == null) {
                errorMessage = "فشل في قراءة الملفات"
                isGenerating = false
                return@launch
            }

            statusMessage = "جارٍ رفع الملفات وتوليد الفيديو (قد يستغرق عدة دقائق)..."

            val repository = NovoraRepository(serverUrl)
            val result = repository.generateVideo(imageFile, audioFile, selectedModel)

            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val url = result.getOrNull()
                    videoUrl = url
                    statusMessage = "تم التوليد بنجاح"
                    
                    // Save to history in background
                    viewModelScope.launch(Dispatchers.IO) {
                        url?.let {
                            historyRepository.insert(
                                HistoryRecord(
                                    videoPath = it,
                                    thumbnailPath = "", // No thumbnail API provided
                                    modelUsed = selectedModel,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "فشل توليد الفيديو، تأكد من الرابط"
                    statusMessage = "فشل التوليد"
                }
                isGenerating = false
            }
        }
    }
}
