package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cyber / Futuristic Dark Palette
val CyberBlack = Color(0xFF060911)
val CyberNavyDark = Color(0xFF0A101F)
val CyberNavyCard = Color(0xFF10192F)
val CyberNavyElevated = Color(0xFF172442)
val CyberNavyBorder = Color(0xFF22355A)

// Vibrant Cyber Accents
val NeonCyan = Color(0xFF00E5FF)
val NeonTurquoise = Color(0xFF00BFA5)
val ElectricBlue = Color(0xFF2979FF)
val CyberPurple = Color(0xFFA855F7)
val CyberPink = Color(0xFFF43F5E)
val CyberRed = Color(0xFFFF1744)
val NeonGreen = Color(0xFF00E676)
val CyberAmber = Color(0xFFFFAB00)

// Text and muted
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Gradients
val CyberGradientPrimary = Brush.linearGradient(
    listOf(NeonTurquoise, NeonCyan, ElectricBlue)
)

val CyberGradientGlow = Brush.radialGradient(
    listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent)
)

val CyberGradientCard = Brush.verticalGradient(
    listOf(CyberNavyElevated, CyberNavyCard)
)

val CyberGradientRed = Brush.linearGradient(
    listOf(CyberRed, CyberPink)
)

val CyberGradientPurple = Brush.linearGradient(
    listOf(CyberPurple, ElectricBlue)
)
