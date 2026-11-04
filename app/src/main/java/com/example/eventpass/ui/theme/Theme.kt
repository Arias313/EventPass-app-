package com.example.eventpass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de color oficial de EventPass (Estética Terminal Pro Oscura,
 * idéntica a la consola web desplegada en Render).
 */
private val EventPassColorScheme = darkColorScheme(
    primary = NuxtSignal,
    onPrimary = MidnightPanel,
    primaryContainer = NuxtSignalSurface,
    onPrimaryContainer = NuxtSignal,
    secondary = NuxtSignalDark,
    onSecondary = SignalWhite,
    secondaryContainer = ElevatedSlate,
    onSecondaryContainer = SignalWhite,
    tertiary = StatusYellow,
    onTertiary = MidnightPanel,
    background = MidnightPanel,
    onBackground = SignalWhite,
    surface = MidnightPanel,
    onSurface = SignalWhite,
    surfaceVariant = DeepSlate,
    onSurfaceVariant = MutedFog,
    surfaceContainerHigh = DeepSlate,
    surfaceContainer = DeepSlate,
    surfaceContainerLow = MidnightPanel,
    outline = SteelOutline,
    outlineVariant = SteelOutline.copy(alpha = 0.6f),
    error = StatusRed,
    onError = Color.White,
    errorContainer = StatusRedContainer,
    onErrorContainer = StatusRed
)

@Composable
fun EventPassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EventPassColorScheme,
        typography = Typography,
        content = content
    )
}
