package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBlack,
    primaryContainer = NeonTurquoise.copy(alpha = 0.2f),
    onPrimaryContainer = NeonCyan,
    secondary = NeonTurquoise,
    onSecondary = CyberBlack,
    secondaryContainer = CyberNavyElevated,
    onSecondaryContainer = NeonTurquoise,
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberNavyDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavyCard,
    onSurfaceVariant = TextSecondary,
    outline = CyberNavyBorder,
    outlineVariant = CyberNavyBorder.copy(alpha = 0.5f),
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MutigandaVpnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
