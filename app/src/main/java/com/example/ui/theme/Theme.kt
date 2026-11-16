package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = ProductGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = TitleOrange,
    onSecondary = Color.White,
    background = WhiteBackground,
    onBackground = WhiteTextPrimary,
    surface = WhiteSurface,
    onSurface = WhiteTextPrimary,
    surfaceVariant = WhiteSurfaceHigh,
    onSurfaceVariant = WhiteTextSecondary,
    outline = WhiteBorder,
    error = TitleRed,
    onError = Color.White,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = ProductGreenBright,
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = TitleOrange,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = TitleRed,
    onError = Color.White,
  )

private val AppShapes =
  Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp),
  )

@Composable
fun MasterJdTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
