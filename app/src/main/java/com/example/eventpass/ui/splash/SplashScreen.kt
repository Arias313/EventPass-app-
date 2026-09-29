package com.example.eventpass.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.eventpass.ui.theme.EventPassTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Temporizador de 2 segundos y luego navega
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToDashboard()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "EventPass",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    EventPassTheme {
        SplashScreen(onNavigateToDashboard = {})
    }
}