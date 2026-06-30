package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = TealPrimary,
    secondary = SlateSecondary,
    tertiary = AccentBlue,
    background = Color(0xFF111827), // deep slate dark background
    surface = Color(0xFF1F2937),
    onPrimary = TealOnPrimary,
    onSecondary = SlateOnSecondary,
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF4B5563)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TealPrimary,
    secondary = SlateSecondary,
    tertiary = AccentBlue,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = TealOnPrimary,
    onSecondary = SlateOnSecondary,
    onBackground = AppOnBackground,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    outline = AppOutline,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = SlateOnSecondaryContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default to false to ensure the Professional Polish theme is shown correctly
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
