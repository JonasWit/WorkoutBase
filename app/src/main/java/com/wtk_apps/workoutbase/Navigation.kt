package com.wtk_apps.workoutbase

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.wtk_apps.workoutbase.ui.settings.SettingsScreen
import com.wtk_apps.workoutbase.ui.stopwatch.StopwatchScreen
import com.wtk_apps.workoutbase.ui.timer.TimerScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)
  val pop: () -> Unit = { backStack.removeLastOrNull() }

  NavDisplay(
    backStack = backStack,
    onBack = pop,
    entryProvider =
      entryProvider {
        entry<Main> {
          TimerScreen(
            onOpenSettings = { backStack.add(Settings) },
            onOpenStopwatch = { backStack.add(Stopwatch) },
          )
        }
        entry<Settings> { SettingsScreen(onBack = pop) }
        entry<Stopwatch> { StopwatchScreen(onBack = pop) }
      },
  )
}
