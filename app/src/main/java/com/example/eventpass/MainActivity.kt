package com.example.eventpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventpass.ui.dashboard.DashboardScreen
import com.example.eventpass.ui.scanner.ScannerScreen
import com.example.eventpass.ui.theme.EventPassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventPassTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            onNavigateToScanner = { navController.navigate("scanner") }
                        )
                    }
                    composable("scanner") {
                        ScannerScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
