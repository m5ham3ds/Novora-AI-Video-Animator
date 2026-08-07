package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.connect.ConnectScreen
import com.example.ui.connect.ConnectViewModel
import com.example.ui.generate.GenerateScreen
import com.example.ui.generate.GenerateViewModel
import com.example.ui.theme.MyApplicationTheme
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovoraApp()
                }
            }
        }
    }
}

@Composable
fun NovoraApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "connect") {
        composable("connect") {
            val connectViewModel: ConnectViewModel = viewModel(factory = ConnectViewModel.Factory)
            ConnectScreen(
                viewModel = connectViewModel,
                onConnected = { url ->
                    val encoded = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("generate/$encoded") {
                        popUpTo("connect") { inclusive = true }
                    }
                }
            )
        }
        composable("generate/{baseUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("baseUrl") ?: ""
            val baseUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            
            val generateViewModel: GenerateViewModel = viewModel(factory = GenerateViewModel.Factory)
            GenerateScreen(
                viewModel = generateViewModel,
                baseUrl = baseUrl,
                onBack = {
                    navController.navigate("connect") {
                        popUpTo("generate") { inclusive = true }
                    }
                }
            )
        }
    }
}
