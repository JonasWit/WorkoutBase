package com.wtk_apps.workoutbase.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WorkoutColorScheme =
  darkColorScheme(
    primary = AccentGreen,
    onPrimary = Ink,
    secondary = RestTeal,
    onSecondary = Ink,
    tertiary = PrepareBlue,
    onTertiary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    outline = Outline1,
  )

/** Fixed, muted dark theme for WorkoutBase. */
@Composable
fun WorkoutBaseTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = WorkoutColorScheme, typography = Typography, content = content)
}
