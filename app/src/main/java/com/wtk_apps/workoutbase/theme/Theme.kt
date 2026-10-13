package com.wtk_apps.workoutbase.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WorkoutColorScheme =
  darkColorScheme(
    primary = AccentGold,
    onPrimary = Ink,
    secondary = RestOrange,
    onSecondary = Ink,
    tertiary = PrepareYellow,
    onTertiary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    outline = Outline1,
  )

/** Fixed, black-and-gold dark theme for WorkoutBase. */
@Composable
fun WorkoutBaseTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = WorkoutColorScheme, typography = Typography, content = content)
}
