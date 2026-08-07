package com.example.ui.generate

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DownloadManager
import android.os.Environment
import android.widget.Toast

import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(permissionsToRequest)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (getFileSize(context, it) <= 5 * 1024 * 1024) {
                viewModel.selectImage(it)
            } else {
                Toast.makeText(context, "الصورة تتجاوز 5 ميجابايت", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (getFileSize(context, it) <= 10 * 1024 * 1024) {
                val name = getFileName(context, it)
                viewModel.selectAudio(it, name ?: "ملف صوتي")
            } else {
                Toast.makeText(context, "الصوت يتجاوز 10 ميجابايت", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("إنشاء فيديو Novora") },
                actions = {
                    IconButton(onClick = { navController.navigate("history") }) {
                        Icon(Icons.Default.History, contentDescription = "سجل الفيديوهات")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                var expanded by remember { mutableStateOf(false) }
                val models = listOf("EchoMimicV3", "V-Express", "SadTalker")
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اختر النموذج") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    viewModel.selectModel(model)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("connect/${viewModel.selectedModel}") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.serverUrl.isNotBlank()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Router, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewModel.serverUrl.isNotBlank()) "متصل بالخادم - تغيير" else "الاتصال بالخادم")
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { imagePicker.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (viewModel.imageUri == null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("اضغط لاختيار صورة للوجه")
                        }
                    } else {
                        AsyncImage(
                            model = viewModel.imageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { audioPicker.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (viewModel.audioUri == null) "اختر ملف صوتي (WAV/MP3)" else "✓ ${viewModel.audioFileName}")
                }
            }

            item {
                Button(
                    onClick = { viewModel.generateVideo(context) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = viewModel.imageUri != null && viewModel.audioUri != null && !viewModel.isGenerating
                ) {
                    Text("بدء إنشاء الفيديو")
                }
            }

            if (viewModel.isGenerating) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                ) {
                    if (viewModel.videoUrl == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("سيظهر الفيديو الناتج هنا")
                        }
                    } else {
                        VideoPlayer(videoUrl = viewModel.videoUrl!!)
                    }
                }
                
                if (viewModel.videoUrl != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        Text(
                            text = "${viewModel.selectedModel} - $date",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = {
                            downloadVideo(context, viewModel.videoUrl!!)
                        }) {
                            Text("تحميل")
                        }
                    }
                }
            }
        }
    }
}

fun getFileSize(context: Context, uri: Uri): Long {
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && sizeIndex != -1) {
            size = cursor.getLong(sizeIndex)
        }
    }
    return size
}

fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
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
