package com.example.ui.connect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel,
    selectedModel: String,
    navController: NavController
) {
    val serverUrl = viewModel.serverUrl
    val isConnecting = viewModel.isConnecting
    val errorMessage = viewModel.errorMessage
    val context = LocalContext.current

    val colabLinks = mapOf(
        "SadTalker" to "https://colab.research.google.com/github/m5ham3ds/SadTalker/blob/main/SadTalker.ipynb",
        "EchoMimicV3" to "https://colab.research.google.com/github/m5ham3ds/echomimic_v3/blob/main/EchoMimicvt.ipynb",
        "V-Express" to "https://colab.research.google.com/github/m5ham3ds/V-Expresss/blob/main/V_Express.ipynb"
    )

    val isError = (errorMessage != null) || (serverUrl.isNotEmpty() && !serverUrl.contains(".gradio.live"))
    val isUrlValid = serverUrl.contains(".gradio.live") && serverUrl.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Router,
                contentDescription = "Router Icon",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ربط الخادم الذكي",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "الصق الرابط الناتج من تشغيل الكود في Google Colab",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { viewModel.updateUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رابط Gradio (مثل https://xxxx.gradio.live)") },
                singleLine = true,
                isError = isError,
                trailingIcon = {
                    if (serverUrl.isNotEmpty()) {
                        if (isUrlValid) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = androidx.compose.ui.graphics.Color.Green)
                        } else {
                            Icon(Icons.Default.Error, contentDescription = "Invalid", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.onConnectClick(navController) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isUrlValid && !isConnecting
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("تأكيد الاتصال بالخادم")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(colabLinks[selectedModel] ?: colabLinks["SadTalker"]))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("تشغيل $selectedModel على Colab")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "إذا لم يكن لديك رابط، شغّل الكود على Colab أولاً",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
