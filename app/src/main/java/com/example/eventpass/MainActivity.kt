package com.example.eventpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.eventpass.ui.dashboard.DashboardScreen
import com.example.eventpass.ui.theme.EventPassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventPassTheme {
                DashboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
