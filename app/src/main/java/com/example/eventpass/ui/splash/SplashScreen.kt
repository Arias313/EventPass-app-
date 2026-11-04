package com.example.eventpass.ui.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventpass.ui.theme.EventPassTheme
import com.example.eventpass.ui.theme.MidnightPanel
import com.example.eventpass.ui.theme.MutedFog
import com.example.eventpass.ui.theme.NuxtSignal
import com.example.eventpass.ui.theme.NuxtSignalSurface
import com.example.eventpass.ui.theme.SignalWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToLogin()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightPanel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(92.dp),
                shape = CircleShape,
                color = NuxtSignalSurface,
                border = BorderStroke(1.5.dp, NuxtSignal.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Logo EventPass",
                        tint = NuxtSignal,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "EventPass",
                color = SignalWhite,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = NuxtSignalSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, NuxtSignal.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "TERMINAL MÓVIL • CONTROL QR",
                    color = NuxtSignal,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Text(
            text = "Plataforma Centralizada • Render & MongoDB Atlas",
            color = MutedFog,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    EventPassTheme {
        SplashScreen(onNavigateToLogin = {})
    }
}
