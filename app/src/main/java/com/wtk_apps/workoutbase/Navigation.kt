package com.wtk_apps.workoutbase

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.wtk_apps.workoutbase.ui.settings.SettingsScreen
import com.wtk_apps.workoutbase.ui.timer.TimerScreen
import com.wtk_apps.workoutbase.ui.trainings.TrainingsScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)
  val context = LocalContext.current
  // Never pop the last entry: pressing system back on the main screen exits the
  // app instead of leaving NavDisplay with an empty back stack (which crashes).
  val pop: () -> Unit = {
    if (backStack.size > 1) backStack.removeLastOrNull() else (context as? Activity)?.finish()
  }

  NavDisplay(
    backStack = backStack,
    onBack = pop,
    entryProvider =
      entryProvider {
        entry<Main> {
          TimerScreen(
            onOpenSettings = { backStack.add(Settings) },
            onOpenTrainings = { backStack.add(Trainings) },
          )
        }
        entry<Settings> { SettingsScreen(onBack = pop) }
        entry<Trainings> { TrainingsScreen(onBack = pop) }
      },
  )
}
