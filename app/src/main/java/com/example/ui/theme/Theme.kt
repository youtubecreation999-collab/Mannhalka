package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
  themeId: String = "whatsapp_classic",
  content: @Composable () -> Unit,
) {
  val palette = ThemePalettes.find { it.id == themeId } ?: ThemePalettes[0]
  val colorScheme = lightColorScheme(
      primary = palette.primary,
      onPrimary = CleanWhite,
      secondary = palette.secondary,
      onSecondary = CleanWhite,
      background = palette.background,
      onBackground = DeepCharcoal,
      surface = CleanWhite,
      onSurface = DeepCharcoal,
      surfaceVariant = palette.background,
      onSurfaceVariant = DeepCharcoal,
      primaryContainer = palette.chatBubbleOut,
      onPrimaryContainer = DeepCharcoal,
      error = ErrorRed,
      outline = TextGray,
      outlineVariant = TextGray
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
