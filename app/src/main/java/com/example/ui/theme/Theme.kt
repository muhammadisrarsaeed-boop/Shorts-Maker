package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighDensityColorScheme = lightColorScheme(
    primary = HdPrimary,
    onPrimary = Color.White,
    primaryContainer = HdPrimaryContainer,
    onPrimaryContainer = HdOnPrimaryContainer,
    secondary = HdPrimary,
    onSecondary = Color.White,
    secondaryContainer = HdBadge,
    onSecondaryContainer = HdOnPrimaryContainer,
    tertiary = HdTextSecondary,
    onTertiary = Color.White,
    background = HdBackground,
    onBackground = HdTextPrimary,
    surface = HdSurface,
    onSurface = HdTextPrimary,
    surfaceVariant = HdSurfaceVariant,
    onSurfaceVariant = HdTextSecondary,
    outline = HdCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighDensityColorScheme,
        typography = Typography,
        content = content
    )
}

