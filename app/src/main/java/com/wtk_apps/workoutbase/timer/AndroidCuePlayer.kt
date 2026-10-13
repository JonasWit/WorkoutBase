package com.wtk_apps.workoutbase.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.wtk_apps.workoutbase.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays timer cues from bundled raw sounds using [SoundPool] and drives the
 * system vibrator.
 *
 * Sounds are played on the media stream (which users normally keep audible)
 * rather than the alarm stream, so cues are reliably heard during training.
 */
class AndroidCuePlayer(context: Context) : CuePlayer {

  private val appContext = context.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val soundPool: SoundPool
  private val beepId: Int
  private val gongId: Int
  @Volatile private var loaded = false

  private val vibrator: Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
      @Suppress("DEPRECATION") appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

  init {
    val attributes =
      AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    soundPool = SoundPool.Builder().setMaxStreams(MAX_STREAMS).setAudioAttributes(attributes).build()
    soundPool.setOnLoadCompleteListener { _, _, status -> if (status == 0) loaded = true }
    beepId = soundPool.load(appContext, R.raw.beep, 1)
    gongId = soundPool.load(appContext, R.raw.gong, 1)
  }

  override fun play(cue: TimerCue, config: TimerConfig) {
    when (cue) {
      TimerCue.PREPARE_START -> {
        sound(beepId, config)
        vibrate(120L, config)
      }
      TimerCue.WORK_START -> {
        sound(gongId, config)
        vibrate(400L, config)
      }
      TimerCue.WORK_END -> {
        sound(gongId, config)
        vibrate(400L, config)
      }
      TimerCue.HALF_WAY -> {
        sound(beepId, config)
        vibrate(120L, config)
      }
      TimerCue.COUNTDOWN -> {
        sound(beepId, config)
        vibrate(80L, config)
      }
      TimerCue.FINISHED -> {
        vibrate(700L, config)
        scope.launch {
          repeat(3) { index ->
            sound(gongId, config)
            if (index < 2) delay(700L)
          }
        }
      }
    }
  }

  private fun sound(soundId: Int, config: TimerConfig) {
    if (!config.soundEnabled) return
    if (loaded) {
      runCatching { soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1f) }
    } else {
      // The very first cue may arrive before SoundPool finished loading; retry once.
      scope.launch {
        delay(150L)
        if (loaded) runCatching { soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1f) }
      }
    }
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
    const val MAX_STREAMS = 4
    const val VOLUME = 1.0f
  }
}
