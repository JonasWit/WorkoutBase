package com.wtk_apps.workoutbase.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * App-scoped holder that drives the pure [TimerEngine] using wall-clock time and
 * forwards cues to a [CuePlayer].
 *
 * Keeping the running loop here (rather than in a ViewModel) means the timer
 * keeps counting while a foreground [com.wtk_apps.workoutbase.service.TimerService]
 * keeps the process alive with the screen off.
 */
object TimerController {

  private const val TICK_MILLIS = 100L

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val _state = MutableStateFlow(TimerEngine.initialState(TimerConfig()))

  /** Observable timer state for the UI and the foreground notification. */
  val state: StateFlow<TimerState> = _state.asStateFlow()

  /** Content observer that should be notified when timer state changes. */
  var cuePlayer: CuePlayer? = null

  /** Replaces the configuration, resetting the session unless one is running. */
  fun configure(config: TimerConfig) {
    _state.value =
      if (_state.value.running) _state.value.copy(config = config) else TimerEngine.reset(config)
  }

  fun toggle() = apply(TimerEngine.toggle(_state.value))

  fun reset() = apply(TimerTransition(TimerEngine.reset(_state.value.config)))

  fun skip() = apply(TimerEngine.skip(_state.value))

  /** Adds [deltaMillis] to the current phase (used by the +/- buttons). */
  fun adjust(deltaMillis: Long) {
    _state.value = TimerEngine.adjustRemaining(_state.value, deltaMillis)
  }

  private fun apply(transition: TimerTransition) {
    transition.cues.forEach { cuePlayer?.play(it, _state.value.config) }
    _state.value = transition.state
    syncLoop()
  }

  private fun syncLoop() {
    if (_state.value.running) startLoop() else stopLoop()
  }

  private fun startLoop() {
    if (loopJob?.isActive == true) return
    lastTickNanos = System.nanoTime()
    loopJob = scope.launch {
      while (isActive) {
        delay(TICK_MILLIS)
        val now = System.nanoTime()
        val deltaMillis = (now - lastTickNanos) / 1_000_000L
        lastTickNanos = now
        val transition = TimerEngine.tick(_state.value, deltaMillis)
        transition.cues.forEach { cuePlayer?.play(it, _state.value.config) }
        _state.value = transition.state
        if (!transition.state.running) break
      }
    }
  }

  private fun stopLoop() {
    loopJob?.cancel()
    loopJob = null
  }

  private var loopJob: Job? = null
  private var lastTickNanos = 0L
}
