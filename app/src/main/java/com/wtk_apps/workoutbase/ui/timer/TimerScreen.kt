package com.wtk_apps.workoutbase.ui.timer

import android.graphics.RuntimeShader
import android.os.Build
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wtk_apps.workoutbase.service.TimerService
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.PrepareBlue
import com.wtk_apps.workoutbase.theme.RestGreen
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
    FlameBackground(color = phaseColor)

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
      colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = MaterialTheme.colorScheme.onPrimary),
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
      TimerPhase.REST -> RestGreen
      TimerPhase.PREPARE -> PrepareBlue
      TimerPhase.IDLE, TimerPhase.FINISHED -> AccentGold
    }
  return animateColorAsState(target, tween(400), label = "phaseColor").value
}

/**
 * Animated flame tongues rising from the bottom of the screen, tinted with the
 * phase colour. Purely procedural: flickering vertical-gradient ovals combined
 * with additive blending so overlapping flames glow.
 */
/**
 * Natural-looking fire drawn with an AGSL fragment shader (Android 13+). On
 * older devices it falls back to the procedural flame shapes.
 */
@Composable
private fun FlameBackground(color: Color) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    FireShaderBackground(color)
  } else {
    ProceduralFlameBackground(color)
  }
}

private const val FIRE_AGSL =
  """
uniform float2 uResolution;
uniform float uTime;
uniform float uColorR;
uniform float uColorG;
uniform float uColorB;

float hash21(float2 p) {
  p = fract(p * float2(123.34, 456.21));
  p += dot(p, p + 45.32);
  return fract(p.x * p.y);
}

float noise(float2 p) {
  float2 i = floor(p);
  float2 f = fract(p);
  float a = hash21(i);
  float b = hash21(i + float2(1.0, 0.0));
  float c = hash21(i + float2(0.0, 1.0));
  float d = hash21(i + float2(1.0, 1.0));
  float2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(float2 p) {
  float v = 0.0;
  float amp = 0.5;
  for (int i = 0; i < 4; i++) {
    v += amp * noise(p);
    p *= 2.0;
    amp *= 0.5;
  }
  return v;
}

half4 main(float2 fragCoord) {
  float3 uColor = float3(uColorR, uColorG, uColorB);
  float2 uv = fragCoord / uResolution;
  uv.y = 1.0 - uv.y;                       // y = 0 at the bottom
  float aspect = uResolution.x / uResolution.y;

  float2 p = float2(uv.x * aspect * 2.6, uv.y * 3.2);
  p.y -= uTime * 1.35;                      // flames rise
  float warp = fbm(p * 0.8 + float2(0.0, uTime * 0.5));
  float n = fbm(p + float2(warp * 1.3, 0.0));

  // Large, strong fire rising from the bottom of the screen.
  float heightMask = smoothstep(1.0, 0.05, uv.y);
  float fire = pow(n * heightMask, 1.6) * 2.0;

  float t = clamp(fire, 0.0, 1.0);
  float3 col = uColor * t * 1.6;
  col += float3(1.0, 0.86, 0.55) * pow(t, 3.0) * 0.7;   // hot core
  col += float3(0.035, 0.032, 0.03);                     // faint ambient

  return half4(col, 1.0);
}
"""

@Composable
private fun FireShaderBackground(color: Color) {
  val transition = rememberInfiniteTransition(label = "fire")
  val clock by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 3600f,
      animationSpec = infiniteRepeatable(tween(3_600_000, easing = LinearEasing), RepeatMode.Restart),
      label = "fireClock",
    )
  val shader = remember { RuntimeShader(FIRE_AGSL) }
  val r = color.red
  val g = color.green
  val b = color.blue

  Canvas(modifier = Modifier.fillMaxSize()) {
    shader.setFloatUniform("uResolution", size.width, size.height)
    shader.setFloatUniform("uTime", clock)
    shader.setFloatUniform("uColorR", r)
    shader.setFloatUniform("uColorG", g)
    shader.setFloatUniform("uColorB", b)
    drawRect(brush = ShaderBrush(shader), size = size)
  }
}

@Composable
private fun ProceduralFlameBackground(color: Color) {
  val transition = rememberInfiniteTransition(label = "flames")
  val clock by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * Math.PI.toFloat()),
      animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
      label = "flameClock",
    )

  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // Warm glow along the bottom edge.
    drawRect(
      brush = Brush.verticalGradient(listOf(Color.Transparent, color.copy(alpha = 0.12f)), startY = h * 0.80f, endY = h),
      topLeft = Offset(0f, h * 0.80f),
      size = Size(w, h * 0.20f),
      blendMode = BlendMode.Plus,
    )

    val flameCount = 12
    for (i in 0 until flameCount) {
      val base = (i + 0.5f) / flameCount + (hash(i * 5.1f) - 0.5f) * 0.05f
      val speed = 0.8f + hash(i * 1.7f) * 1.4f
      val phase = hash(i * 9.3f) * 2f * Math.PI.toFloat()
      val angle = clock * speed + phase
      val flick = 0.55f + 0.45f * kotlin.math.sin(angle)
      val flameHeight = h * (0.16f + 0.18f * hash(i * 3.3f)) * (0.75f + 0.5f * flick)
      val flameWidth = w * (0.10f + 0.06f * hash(i * 2.9f))
      val lean = kotlin.math.sin(angle * 0.7f) * w * 0.03f
      val x = w * base
      val alpha = 0.10f + 0.16f * flick
      val top = h - flameHeight
      val path =
        Path().apply {
          moveTo(x - flameWidth / 2f, h)
          cubicTo(x - flameWidth * 0.45f, h - flameHeight * 0.35f, x - flameWidth * 0.2f + lean * 0.5f, top + flameHeight * 0.3f, x + lean, top)
          cubicTo(x + flameWidth * 0.2f + lean * 0.5f, top + flameHeight * 0.3f, x + flameWidth * 0.45f, h - flameHeight * 0.35f, x + flameWidth / 2f, h)
          close()
        }
      drawPath(
        path = path,
        brush =
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = alpha * 0.55f), color.copy(alpha = alpha)),
            startY = top,
            endY = h,
          ),
        blendMode = BlendMode.Plus,
      )
    }
  }
}

private fun hash(n: Float): Float {
  val x = kotlin.math.sin(n) * 43758.5453f
  return x - kotlin.math.floor(x)
}
