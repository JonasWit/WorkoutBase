package com.wtk_apps.workoutbase.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wtk_apps.workoutbase.timer.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workoutbase_settings")

/**
 * Persists the active [TimerConfig] and up to [MAX_PRESETS] saved trainings with
 * Preferences DataStore, so both survive an app restart.
 */
class SettingsRepository(context: Context) {

  private val dataStore = context.applicationContext.dataStore

  /** The currently active configuration. */
  val config: Flow<TimerConfig> = dataStore.data.map { it.toTimerConfig() }

  /** Saved trainings, in the order they were added. */
  val presets: Flow<List<TrainingPreset>> =
    dataStore.data.map { prefs ->
      val count = (prefs[KEY_PRESET_COUNT] ?: 0).coerceIn(0, MAX_PRESETS)
      (0 until count).mapNotNull { index -> prefs.readPreset(index) }
    }

  suspend fun save(config: TimerConfig) {
    dataStore.edit { it.applyConfig(config) }
  }

  /**
   * One-time migration: the "beep N seconds before the round ends" warning used
   * to be on by default. Turn it off for existing installs so only the 3-2-1
   * countdown is heard, while still letting the user re-enable it in Settings.
   */
  suspend fun applyMigrations() {
    dataStore.edit { prefs ->
      if (prefs[KEY_MIGRATION_WARN_OFF] != true) {
        prefs[KEY_MIGRATION_WARN_OFF] = true
        prefs[KEY_WARN_ENABLED] = false
      }
    }
  }

  /** Stores [preset] if there is room; returns false when the limit is reached. */
  suspend fun addPreset(preset: TrainingPreset): Boolean {
    var added = false
    dataStore.edit { prefs ->
      val count = (prefs[KEY_PRESET_COUNT] ?: 0).coerceIn(0, MAX_PRESETS)
      if (count < MAX_PRESETS) {
        prefs.writePreset(count, preset)
        prefs[KEY_PRESET_COUNT] = count + 1
        added = true
      }
    }
    return added
  }

  suspend fun deletePreset(index: Int) {
    dataStore.edit { prefs ->
      val count = (prefs[KEY_PRESET_COUNT] ?: 0).coerceIn(0, MAX_PRESETS)
      if (index in 0 until count) {
        for (i in index until count - 1) {
          prefs.readPreset(i + 1)?.let { prefs.writePreset(i, it) } ?: prefs.clearPreset(i)
        }
        prefs.clearPreset(count - 1)
        prefs[KEY_PRESET_COUNT] = count - 1
      }
    }
  }

  // --- Active config mapping -------------------------------------------------

  private fun Preferences.toTimerConfig(): TimerConfig {
    val defaults = TimerConfig()
    return runCatching { readConfigFields(defaults) }.getOrDefault(defaults)
  }

  private fun Preferences.readConfigFields(defaults: TimerConfig): TimerConfig =
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

  // --- Preset mapping --------------------------------------------------------

  private fun Preferences.readPreset(index: Int): TrainingPreset? {
    val name = this[pName(index)] ?: return null
    val defaults = TimerConfig()
    val config =
      runCatching {
          TimerConfig(
            rounds = (this[pRounds(index)] ?: defaults.rounds).coerceIn(1, 99),
            workMillis = (this[pWork(index)] ?: defaults.seconds(defaults.workMillis)).coerceIn(1, 3_600) * 1_000L,
            restMillis = (this[pRest(index)] ?: defaults.seconds(defaults.restMillis)).coerceIn(0, 3_600) * 1_000L,
            prepareMillis = (this[pPrepare(index)] ?: defaults.seconds(defaults.prepareMillis)).coerceIn(0, 600) * 1_000L,
            warnMillis = (this[pWarn(index)] ?: defaults.seconds(defaults.warnMillis)).coerceIn(0, 600) * 1_000L,
            countdownSeconds = (this[pCountdown(index)] ?: defaults.countdownSeconds).coerceIn(0, 10),
            halfWayEnabled = this[pHalfWay(index)] ?: defaults.halfWayEnabled,
            warnEnabled = this[pWarnEnabled(index)] ?: defaults.warnEnabled,
            countdownEnabled = this[pCountdownEnabled(index)] ?: defaults.countdownEnabled,
            vibrationEnabled = this[pVibration(index)] ?: defaults.vibrationEnabled,
            soundEnabled = this[pSound(index)] ?: defaults.soundEnabled,
          )
        }
        .getOrDefault(defaults)
    return TrainingPreset(name, config)
  }

  private fun MutablePreferences.writePreset(index: Int, preset: TrainingPreset) {
    this[pName(index)] = preset.name
    this[pRounds(index)] = preset.config.rounds
    this[pWork(index)] = preset.config.seconds(preset.config.workMillis)
    this[pRest(index)] = preset.config.seconds(preset.config.restMillis)
    this[pPrepare(index)] = preset.config.seconds(preset.config.prepareMillis)
    this[pWarn(index)] = preset.config.seconds(preset.config.warnMillis)
    this[pCountdown(index)] = preset.config.countdownSeconds
    this[pHalfWay(index)] = preset.config.halfWayEnabled
    this[pWarnEnabled(index)] = preset.config.warnEnabled
    this[pCountdownEnabled(index)] = preset.config.countdownEnabled
    this[pVibration(index)] = preset.config.vibrationEnabled
    this[pSound(index)] = preset.config.soundEnabled
  }

  private fun MutablePreferences.clearPreset(index: Int) {
    remove(pName(index))
    remove(pRounds(index))
    remove(pWork(index))
    remove(pRest(index))
    remove(pPrepare(index))
    remove(pWarn(index))
    remove(pCountdown(index))
    remove(pHalfWay(index))
    remove(pWarnEnabled(index))
    remove(pCountdownEnabled(index))
    remove(pVibration(index))
    remove(pSound(index))
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
    val KEY_PRESET_COUNT = intPreferencesKey("preset_count")
    val KEY_MIGRATION_WARN_OFF = booleanPreferencesKey("migration_warn_off_v1")

    fun pName(index: Int) = stringPreferencesKey("preset_" + index + "_name")
    fun pRounds(index: Int) = intPreferencesKey("preset_" + index + "_rounds")
    fun pWork(index: Int) = intPreferencesKey("preset_" + index + "_work")
    fun pRest(index: Int) = intPreferencesKey("preset_" + index + "_rest")
    fun pPrepare(index: Int) = intPreferencesKey("preset_" + index + "_prepare")
    fun pWarn(index: Int) = intPreferencesKey("preset_" + index + "_warn")
    fun pCountdown(index: Int) = intPreferencesKey("preset_" + index + "_countdown")
    fun pHalfWay(index: Int) = booleanPreferencesKey("preset_" + index + "_halfway")
    fun pWarnEnabled(index: Int) = booleanPreferencesKey("preset_" + index + "_warn_enabled")
    fun pCountdownEnabled(index: Int) = booleanPreferencesKey("preset_" + index + "_countdown_enabled")
    fun pVibration(index: Int) = booleanPreferencesKey("preset_" + index + "_vibration")
    fun pSound(index: Int) = booleanPreferencesKey("preset_" + index + "_sound")
  }
}
