package com.wtk_apps.workoutbase.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wtk_apps.workoutbase.service.TimerService
import com.wtk_apps.workoutbase.theme.AccentGreen
import com.wtk_apps.workoutbase.theme.PrepareBlue
import com.wtk_apps.workoutbase.theme.RestTeal
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.theme.workProgressColor
import com.wtk_apps.workoutbase.timer.TimerController
import com.wtk_apps.workoutbase.timer.TimerPhase
import com.wtk_apps.workoutbase.timer.TimerState
import com.wtk_apps.workoutbase.timer.formatMillis
import com.wtk_apps.workoutbase.timer.label

@Composable
fun TimerScreen(
  onOpenSettings: () -> Unit,
  onOpenStopwatch: () -> Unit,
  onOpenTrainings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by TimerController.state.collectAsStateWithLifecycle()
  val context = LocalContext.current

  // Keep the timer alive with a foreground service while it is running.
  LaunchedEffect(state.running) {
    if (state.running) TimerService.start(context) else TimerService.stop(context)
  }

  val phaseColor = phaseColor(state)
  val ringProgress by
    animateFloatAsState(
      targetValue = state.progress,
      animationSpec = tween(300, easing = LinearEasing),
      label = "ring",
    )

  Box(modifier = modifier.fillMaxSize()) {
    BreathingGlow(color = phaseColor, active = state.running)

    // Top bar, pinned to the top edge.
    Row(
      modifier =
        Modifier.align(Alignment.TopCenter)
          .fillMaxWidth()
          .safeDrawingPadding()
          .padding(horizontal = 24.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("WorkoutBase", color = TextPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
      Spacer(Modifier.weight(1f))
      TextButton(onClick = onOpenSettings) { Text("Settings", color = TextSecondary) }
    }

    // Phase label + dial, centred on the screen.
    Column(
      modifier = Modifier.align(Alignment.Center).offset(y = (-28).dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      PhaseHeader(state = state, color = phaseColor)
      Spacer(Modifier.height(24.dp))
      TimerDial(state = state, color = phaseColor, progress = ringProgress)
    }

    // Controls, pinned to the bottom edge.
    Column(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .safeDrawingPadding()
          .padding(horizontal = 24.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Controls(state = state)
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TextButton(onClick = onOpenTrainings) {
          Text("Trainings", color = TextSecondary, letterSpacing = 1.sp)
        }
        TextButton(onClick = onOpenStopwatch) {
          Text("Stopwatch", color = TextSecondary, letterSpacing = 1.sp)
        }
      }
    }
  }
}

@Composable
private fun PhaseHeader(state: TimerState, color: Color) {
  Text(
    text = state.phase.label(),
    color = color,
    fontSize = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 6.sp,
  )
  if (state.round > 0) {
    Spacer(Modifier.height(4.dp))
    Text(
      text = "ROUND ${state.round} / ${state.config.rounds}",
      color = TextSecondary,
      fontSize = 14.sp,
      letterSpacing = 3.sp,
    )
  }
}

@Composable
private fun TimerDial(state: TimerState, color: Color, progress: Float) {
  Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val stroke = 14.dp.toPx()
      val inset = stroke / 2f
      val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
      drawArc(
        color = Surface2,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(inset, inset),
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
      )
      drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = Offset(inset, inset),
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
      )
    }
    Text(
      text = formatMillis(state.remainingMillis),
      color = color,
      fontSize = 84.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
    )
  }
}

@Composable
private fun Controls(state: TimerState) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    OutlinedButton(onClick = { TimerController.reset() }) { Text("Reset") }
    val primaryLabel =
      when {
        state.running -> "PAUSE"
        state.phase == TimerPhase.FINISHED -> "AGAIN"
        state.phase == TimerPhase.IDLE -> "START"
        else -> "RESUME"
      }
    Button(
      onClick = { TimerController.toggle() },
      modifier = Modifier.weight(1f).height(56.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text(primaryLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
    OutlinedButton(onClick = { TimerController.skip() }) { Text("Skip") }
  }
}

@Composable
private fun phaseColor(state: TimerState): Color {
  val target =
    when (state.phase) {
      TimerPhase.WORK -> workProgressColor(state.progress)
      TimerPhase.REST -> RestTeal
      TimerPhase.PREPARE -> PrepareBlue
      TimerPhase.IDLE, TimerPhase.FINISHED -> AccentGreen
    }
  return animateColorAsState(target, tween(400), label = "phaseColor").value
}

@Composable
private fun BreathingGlow(color: Color, active: Boolean) {
  val transition = rememberInfiniteTransition(label = "glow")
  val pulse by
    transition.animateFloat(
      initialValue = 0.18f,
      targetValue = 0.45f,
      animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
      label = "pulse",
    )
  val alpha = if (active) pulse else 0.18f
  Canvas(modifier = Modifier.fillMaxSize()) {
    val radius = size.minDimension * 0.85f
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(color.copy(alpha = alpha), Color.Transparent),
          center = center,
          radius = radius,
        ),
      radius = radius,
      center = center,
    )
  }
}
