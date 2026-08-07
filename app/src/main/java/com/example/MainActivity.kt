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
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.theme.MyApplicationTheme

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

    NavHost(navController = navController, startDestination = "generate") {
        composable("generate") {
            val generateViewModel: GenerateViewModel = viewModel(factory = GenerateViewModel.Factory)
            GenerateScreen(
                viewModel = generateViewModel,
                navController = navController
            )
        }
        composable("connect/{selectedModel}") { backStackEntry ->
            val selectedModel = backStackEntry.arguments?.getString("selectedModel") ?: "SadTalker"
            val connectViewModel: ConnectViewModel = viewModel(factory = ConnectViewModel.Factory)
            ConnectScreen(
                viewModel = connectViewModel,
                selectedModel = selectedModel,
                navController = navController
            )
        }
        composable("history") {
            val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
            HistoryScreen(
                viewModel = historyViewModel,
                navController = navController
            )
        }
    }
}
