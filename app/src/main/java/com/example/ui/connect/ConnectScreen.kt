package com.example.ui.connect

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel,
    onConnected: (String) -> Unit
) {
    val url by viewModel.url.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(isConnected) {
        if (isConnected && url.isNotBlank()) {
            Toast.makeText(context, "تم الاتصال بنجاح", Toast.LENGTH_SHORT).show()
            onConnected(url)
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Connect to AI Server") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("أدخل رابط خادم Gradio للبدء", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = url,
                onValueChange = viewModel::onUrlChange,
                label = { Text("Server URL (e.g. https://xxx.gradio.live)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::connect,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("connect_button")
            ) {
                Text("Connect")
            }
        }
    }
}
