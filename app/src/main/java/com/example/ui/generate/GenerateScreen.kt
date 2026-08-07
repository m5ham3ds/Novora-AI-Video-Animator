package com.example.ui.generate

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel,
    baseUrl: String,
    onBack: () -> Unit
) {
    val imageUri by viewModel.imageUri.collectAsStateWithLifecycle()
    val audioUri by viewModel.audioUri.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setImageUri(uri) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setAudioUri(uri) }

    val models = listOf("EchoMimicV3", "V-Express", "SadTalker")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Your Video") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("رجوع") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Model Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اختر النموذج") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                viewModel.setSelectedModel(model)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File Pickers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { imagePicker.launch("image/*") }) {
                    Text(if (imageUri != null) "الصورة ✅" else "رفع صورة")
                }
                Button(onClick = { audioPicker.launch("audio/*") }) {
                    Text(if (audioUri != null) "الصوت ✅" else "رفع صوت")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Button
            Button(
                onClick = { viewModel.generateVideo(baseUrl) },
                enabled = state != GenerateState.LOADING,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_button")
            ) {
                Text("Generate Video")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status and Results
            when (state) {
                GenerateState.LOADING -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("جارٍ توليد الفيديو، قد يستغرق هذا عدة دقائق...")
                }
                GenerateState.ERROR -> {
                    Text(text = "خطأ: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
                GenerateState.SUCCESS -> {
                    Text("تم التوليد بنجاح!", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (videoUrl != null) {
                        VideoPlayer(videoUrl = videoUrl!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = {
                                downloadVideo(context, videoUrl!!)
                            }) {
                                Text("تنزيل الفيديو")
                            }
                            Button(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                context.startActivity(intent)
                            }) {
                                Text("فتح في المتصفح")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

fun downloadVideo(context: Context, url: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Novora Video")
            .setDescription("Downloading generated video...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "novora_video_${System.currentTimeMillis()}.mp4"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "بدأ التحميل...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "فشل بدء التحميل", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}
