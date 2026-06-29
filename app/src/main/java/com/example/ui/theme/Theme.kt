package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SophisticatedAccent,
    secondary = SophisticatedAccentSecondary,
    tertiary = SophisticatedBorder,
    background = SophisticatedBg,
    surface = SophisticatedSurface,
    onPrimary = SophisticatedBg,
    onSecondary = SophisticatedTextPrimary,
    onTertiary = SophisticatedTextPrimary,
    onBackground = SophisticatedTextPrimary,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedSurfaceDark,
    onSurfaceVariant = SophisticatedTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to a premium dark mode for focus focus focus!
    dynamicColor: Boolean = false, // Keep consistent branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
