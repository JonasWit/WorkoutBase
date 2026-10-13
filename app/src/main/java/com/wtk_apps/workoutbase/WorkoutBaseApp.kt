package com.wtk_apps.workoutbase

import android.app.Application
import com.wtk_apps.workoutbase.data.SettingsRepository
import com.wtk_apps.workoutbase.timer.AndroidCuePlayer
import com.wtk_apps.workoutbase.timer.TimerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Minimal service locator so screens can reach shared singletons. */
object AppGraph {
  lateinit var settings: SettingsRepository
    private set

  fun init(context: Application) {
    settings = SettingsRepository(context)
  }
}

class WorkoutBaseApp : Application() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  override fun onCreate() {
    super.onCreate()
    AppGraph.init(this)
    TimerController.cuePlayer = AndroidCuePlayer(this)
    scope.launch {
      val config = AppGraph.settings.config.first()
      TimerController.configure(config)
    }
  }
}
