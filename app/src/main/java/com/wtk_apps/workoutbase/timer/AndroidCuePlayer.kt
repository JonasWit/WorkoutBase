package com.wtk_apps.workoutbase.timer

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Playback of timer cues using [ToneGenerator] for tones and the system
 * vibrator for haptics. Sound and vibration can be disabled through
 * [TimerConfig.soundEnabled] / [TimerConfig.vibrationEnabled].
 */
class AndroidCuePlayer(context: Context) : CuePlayer {

  private val appContext = context.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val toneGenerator: ToneGenerator? =
    runCatching { ToneGenerator(AudioManager.STREAM_ALARM, VOLUME) }.getOrNull()

  private val vibrator: Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
      @Suppress("DEPRECATION") appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

  override fun play(cue: TimerCue, config: TimerConfig) {
    when (cue) {
      TimerCue.PREPARE_START -> {
        tone(ToneGenerator.TONE_PROP_BEEP2, 300, config)
        vibrate(120L, config)
      }
      TimerCue.WORK_START -> {
        gong(config)
        vibrate(400L, config)
      }
      TimerCue.WORK_END -> {
        gong(config)
        vibrate(400L, config)
      }
      TimerCue.HALF_WAY -> {
        tone(ToneGenerator.TONE_PROP_BEEP, 200, config)
        vibrate(120L, config)
      }
      TimerCue.WARN -> {
        vibrate(120L, config)
        scope.launch {
          tone(ToneGenerator.TONE_PROP_BEEP, 160, config)
          delay(200L)
          tone(ToneGenerator.TONE_PROP_BEEP, 160, config)
        }
      }
      TimerCue.COUNTDOWN -> {
        tone(ToneGenerator.TONE_PROP_BEEP, 140, config)
        vibrate(80L, config)
      }
      TimerCue.FINISHED -> {
        vibrate(700L, config)
        scope.launch {
          repeat(3) { index ->
            gong(config)
            if (index < 2) delay(650L)
          }
        }
      }
    }
  }

  private fun gong(config: TimerConfig) {
    tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 700, config)
  }

  private fun tone(tone: Int, durationMs: Int, config: TimerConfig) {
    if (!config.soundEnabled) return
    runCatching { toneGenerator?.startTone(tone, durationMs) }
  }

  private fun vibrate(durationMs: Long, config: TimerConfig) {
    if (!config.vibrationEnabled) return
    val v = vibrator ?: return
    if (!v.hasVibrator()) return
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION") v.vibrate(durationMs)
      }
    }
  }

  private companion object {
    const val VOLUME = 90
  }
}
