package com.wtk_apps.workoutbase.timer

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the total-session and total-remaining time helpers. */
class TotalTimeTest {

  private fun config(
    rounds: Int = 3,
    workMillis: Long = 10_000L,
    restMillis: Long = 5_000L,
    prepareMillis: Long = 5_000L,
  ) = TimerConfig(
    rounds = rounds,
    workMillis = workMillis,
    restMillis = restMillis,
    prepareMillis = prepareMillis,
  )

  @Test
  fun totalSession_countsPrepareWorkAndRestsBetweenRounds() {
    // 5 + 3*10 + 2*5 = 45 s
    assertEquals(45_000L, totalSessionMillis(config()))
  }

  @Test
  fun idle_remainingEqualsTotalSession() {
    val state = TimerEngine.initialState(config())
    assertEquals(45_000L, totalRemainingMillis(state))
  }

  @Test
  fun prepare_remainingCountsPrepareLeftPlusAllRounds() {
    val started = TimerEngine.start(TimerEngine.initialState(config())).state
    val twoSecondsIn = TimerEngine.tick(started, 2_000L).state
    // prepare left 3s + 3*10 + 2*5 = 43 s
    assertEquals(43_000L, totalRemainingMillis(twoSecondsIn))
  }

  @Test
  fun work_remainingIncludesRestAndLaterRounds() {
    val started = TimerEngine.start(TimerEngine.initialState(config(prepareMillis = 0L))).state
    val fourSecondsIn = TimerEngine.tick(started, 4_000L).state
    // 6s left in round 1 + rest 5 + round2 10 + rest 5 + round3 10 = 36 s
    assertEquals(36_000L, totalRemainingMillis(fourSecondsIn))
  }

  @Test
  fun lastWorkRound_hasNoRestAfterIt() {
    val started = TimerEngine.start(TimerEngine.initialState(config(rounds = 1, prepareMillis = 0L))).state
    val eightSecondsIn = TimerEngine.tick(started, 8_000L).state
    // only 2s left, no rest afterwards
    assertEquals(2_000L, totalRemainingMillis(eightSecondsIn))
  }

  @Test
  fun finished_hasZeroRemaining() {
    val started = TimerEngine.start(TimerEngine.initialState(config(rounds = 1, prepareMillis = 0L))).state
    val finished = TimerEngine.tick(started, 10_000L).state
    assertEquals(TimerPhase.FINISHED, finished.phase)
    assertEquals(0L, totalRemainingMillis(finished))
  }
}
