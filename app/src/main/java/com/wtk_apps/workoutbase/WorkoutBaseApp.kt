package com.wtk_apps.workoutbase

import android.app.Application
import com.wtk_apps.workoutbase.data.SettingsRepository
import com.wtk_apps.workoutbase.timer.AndroidCuePlayer
import com.wtk_apps.workoutbase.timer.MonotonicClock
import com.wtk_apps.workoutbase.timer.TimerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Process-wide wiring. Exists only because the app has no dependency-injection setup;
 * every dependency here is created once, in one place, and must not be re-assigned later.
 */
object AppGraph {
  lateinit var settings: SettingsRepository
    private set

  lateinit var timer: TimerController
    private set

  fun init(context: Application) {
    settings = SettingsRepository(context)
    timer =
      TimerController(
        cuePlayer = AndroidCuePlayer(context),
        clock = MonotonicClock { System.nanoTime() / 1_000_000L },
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
      )
  }
}

class WorkoutBaseApp : Application() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  override fun onCreate() {
    super.onCreate()
    AppGraph.init(this)
    scope.launch {
      val config = AppGraph.settings.config.first()
      AppGraph.timer.configure(config)
    }
  }
}
