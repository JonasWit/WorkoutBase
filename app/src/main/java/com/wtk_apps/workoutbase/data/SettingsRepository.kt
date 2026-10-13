package com.wtk_apps.workoutbase.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wtk_apps.workoutbase.timer.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workoutbase_settings")

/**
 * Persists the [TimerConfig] with Preferences DataStore so the last used
 * settings survive an app restart.
 */
class SettingsRepository(context: Context) {

  private val dataStore = context.applicationContext.dataStore

  val config: Flow<TimerConfig> = dataStore.data.map { it.toTimerConfig() }

  suspend fun save(config: TimerConfig) {
    dataStore.edit { it.applyConfig(config) }
  }

  private fun Preferences.toTimerConfig(): TimerConfig {
    val defaults = TimerConfig()
    return runCatching {
        TimerConfig(
          rounds = (this[KEY_ROUNDS] ?: defaults.rounds).coerceIn(1, 99),
          workMillis = (this[KEY_WORK_SEC] ?: defaults.seconds(defaults.workMillis)).coerceIn(1, 3_600) * 1_000L,
          restMillis = (this[KEY_REST_SEC] ?: defaults.seconds(defaults.restMillis)).coerceIn(0, 3_600) * 1_000L,
          prepareMillis = (this[KEY_PREPARE_SEC] ?: defaults.seconds(defaults.prepareMillis)).coerceIn(0, 600) * 1_000L,
          warnMillis = (this[KEY_WARN_SEC] ?: defaults.seconds(defaults.warnMillis)).coerceIn(0, 600) * 1_000L,
          countdownSeconds = (this[KEY_COUNTDOWN_SEC] ?: defaults.countdownSeconds).coerceIn(0, 10),
          halfWayEnabled = this[KEY_HALF_WAY] ?: defaults.halfWayEnabled,
          warnEnabled = this[KEY_WARN_ENABLED] ?: defaults.warnEnabled,
          countdownEnabled = this[KEY_COUNTDOWN_ENABLED] ?: defaults.countdownEnabled,
          vibrationEnabled = this[KEY_VIBRATION] ?: defaults.vibrationEnabled,
          soundEnabled = this[KEY_SOUND] ?: defaults.soundEnabled,
        )
      }
      .getOrDefault(defaults)
  }

  private fun MutablePreferences.applyConfig(config: TimerConfig) {
    this[KEY_ROUNDS] = config.rounds
    this[KEY_WORK_SEC] = config.seconds(config.workMillis)
    this[KEY_REST_SEC] = config.seconds(config.restMillis)
    this[KEY_PREPARE_SEC] = config.seconds(config.prepareMillis)
    this[KEY_WARN_SEC] = config.seconds(config.warnMillis)
    this[KEY_COUNTDOWN_SEC] = config.countdownSeconds
    this[KEY_HALF_WAY] = config.halfWayEnabled
    this[KEY_WARN_ENABLED] = config.warnEnabled
    this[KEY_COUNTDOWN_ENABLED] = config.countdownEnabled
    this[KEY_VIBRATION] = config.vibrationEnabled
    this[KEY_SOUND] = config.soundEnabled
  }

  private fun TimerConfig.seconds(millis: Long): Int = (millis / 1_000L).toInt()

  private companion object {
    val KEY_ROUNDS = intPreferencesKey("rounds")
    val KEY_WORK_SEC = intPreferencesKey("work_seconds")
    val KEY_REST_SEC = intPreferencesKey("rest_seconds")
    val KEY_PREPARE_SEC = intPreferencesKey("prepare_seconds")
    val KEY_WARN_SEC = intPreferencesKey("warn_seconds")
    val KEY_COUNTDOWN_SEC = intPreferencesKey("countdown_seconds")
    val KEY_HALF_WAY = booleanPreferencesKey("half_way")
    val KEY_WARN_ENABLED = booleanPreferencesKey("warn_enabled")
    val KEY_COUNTDOWN_ENABLED = booleanPreferencesKey("countdown_enabled")
    val KEY_VIBRATION = booleanPreferencesKey("vibration")
    val KEY_SOUND = booleanPreferencesKey("sound")
  }
}
