package com.example.miestanteria.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = SageSecondary,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    outline = OutlineBorder,
    surfaceVariant = Color(0xFFEFF3F1),
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = SageSecondary,
    onPrimary = TextPrimary,
    primaryContainer = SagePrimaryDark,
    onPrimaryContainer = SageContainer,
    secondary = SageSecondary,
    background = Color(0xFF141917),
    onBackground = Color(0xFFE2E6E4),
    surface = Color(0xFF1E2522),
    onSurface = Color(0xFFE2E6E4),
    outline = Color(0xFF38423E)
)

@Composable
fun MiEstanteriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}