package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun MyApplicationTheme(
  themeId: String = "sunset_coral",
  content: @Composable () -> Unit,
) {
  val palette = ThemePalettes.find { it.id == themeId } ?: ThemePalettes[0]
  val isDark = palette.background.luminance() < 0.5
  val onColor = if (isDark) CleanWhite else DeepCharcoal
  val colorScheme = lightColorScheme(
      primary = palette.primary,
      onPrimary = CleanWhite,
      secondary = palette.secondary,
      onSecondary = CleanWhite,
      background = palette.background,
      onBackground = onColor,
      surface = if (isDark) palette.primary else CleanWhite,
      onSurface = onColor,
      surfaceVariant = palette.background,
      onSurfaceVariant = onColor,
      primaryContainer = palette.chatBubbleOut,
      onPrimaryContainer = onColor,
      error = ErrorRed,
      outline = if (isDark) CleanWhite else TextGray,
      outlineVariant = if (isDark) CleanWhite else TextGray
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
