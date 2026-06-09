package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Enforce our custom branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
