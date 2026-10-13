package com.wtk_apps.workoutbase.timer

/** Formats a duration in milliseconds as `MM:SS`, rounding up to whole seconds. */
fun formatMillis(millis: Long): String {
  val totalSeconds = ((millis + 999L) / 1_000L).coerceAtLeast(0L)
  val minutes = totalSeconds / 60L
  val seconds = totalSeconds % 60L
  return "%02d:%02d".format(minutes, seconds)
}

/**
 * Longest allowed phase duration in seconds (99:59). Keeping minutes to two
 * digits means the clock never exceeds the `MM:SS` format.
 */
const val MAX_PHASE_SECONDS = 99 * 60 + 59

/** Human readable label for a phase (English UI). */
fun TimerPhase.label(): String =
  when (this) {
    TimerPhase.IDLE -> "READY"
    TimerPhase.PREPARE -> "GET READY"
    TimerPhase.WORK -> "WORK"
    TimerPhase.REST -> "REST"
    TimerPhase.FINISHED -> "DONE"
  }
