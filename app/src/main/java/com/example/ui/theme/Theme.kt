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

private val NaturalTonesColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = CleanWhite,
    secondary = EarthBrown,
    onSecondary = CleanWhite,
    background = CreamWhite,
    onBackground = DeepCharcoal,
    surface = CleanWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = WarmSand,
    onSurfaceVariant = EarthBrown,
    error = ErrorRed,
    outline = SoftSandBorder,
    outlineVariant = MutedSageText
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Respect Natural Tones soft light theme as primary
  dynamicColor: Boolean = false, // Enforce brand identity consistently
  content: @Composable () -> Unit,
) {
  val colorScheme = NaturalTonesColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
