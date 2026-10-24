package com.wtk_apps.workoutbase.ui.timer

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wtk_apps.workoutbase.AppGraph
import com.wtk_apps.workoutbase.R
import com.wtk_apps.workoutbase.service.TimerService
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.Ink
import com.wtk_apps.workoutbase.theme.PrepareBlue
import com.wtk_apps.workoutbase.theme.RestGreen
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.WorkYellow
import com.wtk_apps.workoutbase.theme.workProgressColor
import com.wtk_apps.workoutbase.timer.TimerConfig
import com.wtk_apps.workoutbase.timer.TimerPhase
import com.wtk_apps.workoutbase.timer.TimerState
import com.wtk_apps.workoutbase.timer.formatMillis
import com.wtk_apps.workoutbase.timer.label
import com.wtk_apps.workoutbase.timer.sessionSegments
import com.wtk_apps.workoutbase.timer.totalRemainingMillis

/** Roman-inscription style typeface used for the app title. */
private val RomanTitleFont = FontFamily(Font(R.font.cinzel))

@Composable
fun TimerScreen(
  onOpenSettings: () -> Unit,
  onOpenTrainings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by AppGraph.timer.state.collectAsStateWithLifecycle()
  val context = LocalContext.current

  // Keep the timer alive with a foreground service while it is running.
  LaunchedEffect(state.running) {
    if (state.running) TimerService.start(context) else TimerService.stop(context)
  }

  val phaseColor = phaseColor(state)
  // Flames move at a different pace per phase: brisk on work, medium on prepare
  // and slow/calm on rest.
  val flameSpeed =
    when (state.phase) {
      TimerPhase.WORK -> 2.6f
      TimerPhase.PREPARE -> 1.2f
      TimerPhase.REST -> 0.55f
      TimerPhase.IDLE, TimerPhase.FINISHED -> 0.8f
    }
  val ringProgress by
    animateFloatAsState(
      targetValue = state.progress,
      animationSpec = tween(300, easing = LinearEasing),
      label = "ring",
    )

  Box(modifier = modifier.fillMaxSize()) {
    FlameBackground(color = phaseColor, speed = flameSpeed)

    Column(
      modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Roman title, single line.
      Text(
        text = "WORKOUT BASE",
        color = AccentGold,
        fontFamily = RomanTitleFont,
        fontSize = 22.sp,
        letterSpacing = 3.sp,
        modifier = Modifier.padding(top = 8.dp),
      )

      // Phase label + dial, centred in the space between title and controls.
      BoxWithConstraints(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        val dialSize = minOf(maxWidth.value, (maxHeight.value - 150f).coerceAtLeast(170f)).dp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          PhaseHeader(state = state, color = phaseColor, speed = flameSpeed)
          Spacer(Modifier.height(18.dp))
          TimerDial(state = state, color = phaseColor, progress = ringProgress, size = dialSize)
        }
      }

      // Controls + links, pinned to the bottom.
      Controls(state = state)
      Spacer(Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BottomLink("Trainings", onOpenTrainings)
        BottomLink("Settings", onOpenSettings)
      }
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun BottomLink(
  text: String,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .clip(RoundedCornerShape(50))
        .background(Color.Black.copy(alpha = 0.6f))
        .border(1.dp, TextPrimary.copy(alpha = 0.35f), RoundedCornerShape(50))
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 10.dp),
  ) {
    Text(text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
  }
}

@Composable
private fun PhaseHeader(
  state: TimerState,
  color: Color,
  speed: Float,
) {
  Text(
    text = state.phase.label(),
    color = color,
    fontSize = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 6.sp,
  )
  Spacer(Modifier.height(10.dp))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    ParamChip("ROUNDS", state.config.rounds.toString(), AccentGold, Modifier.weight(1f))
    ParamChip("WORK", formatMillis(state.config.workMillis), WorkYellow, Modifier.weight(1f), active = state.phase == TimerPhase.WORK)
    ParamChip("REST", formatMillis(state.config.restMillis), RestGreen, Modifier.weight(1f), active = state.phase == TimerPhase.REST)
    ParamChip(
      "PREP",
      formatMillis(state.config.prepareMillis),
      PrepareBlue,
      Modifier.weight(1f),
      active = state.phase == TimerPhase.PREPARE,
    )
  }
  if (state.round > 0) {
    Spacer(Modifier.height(10.dp))
    Text(
      text = "ROUND ${state.round} / ${state.config.rounds}",
      color = Color.White,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 3.sp,
    )
  }
  Spacer(Modifier.height(14.dp))
  SessionProgressBar(
    config = state.config,
    remainingMillis = totalRemainingMillis(state),
    speed = speed,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
  )
}

/** Segmented session bar: PREPARE / WORK / REST sections split by dividers. */
@Composable
private fun SessionProgressBar(
  config: TimerConfig,
  remainingMillis: Long,
  speed: Float,
  modifier: Modifier = Modifier,
) {
  val segments = remember(config) { sessionSegments(config) }
  val total = remember(segments) { segments.sumOf { it.millis }.coerceAtLeast(1L) }
  val target = ((total - remainingMillis).toFloat() / total).coerceIn(0f, 1f)
  val fill by animateFloatAsState(target, tween(400, easing = LinearEasing), label = "sessionFill")

  // Fire-like shimmer that sweeps across the bar; faster while working.
  val shimmer = remember { mutableFloatStateOf(0f) }
  val currentSpeed = rememberUpdatedState(speed)
  LaunchedEffect(Unit) {
    var last = 0L
    while (true) {
      withFrameNanos { now ->
        if (last != 0L) {
          val dt = (now - last) / 1_000_000_000f
          shimmer.floatValue = (shimmer.floatValue + dt * (0.22f + currentSpeed.value * 0.20f)) % 1f
        }
        last = now
      }
    }
  }

  Canvas(modifier = modifier.height(18.dp)) {
    val w = size.width
    val h = size.height
    val radius = h * 0.25f
    val bar = Path().apply { addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(radius, radius))) }
    val playX = w * fill

    clipPath(bar) {
      var x = 0f
      segments.forEachIndexed { i, seg ->
        val sw = w * (seg.millis.toFloat() / total)
        val c = segmentColor(seg.phase)
        // Dim track for the whole section.
        drawRect(c.copy(alpha = 0.22f), topLeft = Offset(x, 0f), size = Size(sw, h))
        // Elapsed part: flickering, glossy colour like the flames.
        val filled = (minOf(x + sw, playX) - x).coerceAtLeast(0f)
        if (filled > 0f) {
          val flick = 0.5f + 0.5f * kotlin.math.sin(shimmer.floatValue * 6.2831855f * (1f + (i % 3)) + i * 1.7f)
          val bright = lerp(c, Color.White, 0.10f + 0.28f * flick)
          drawRect(
            brush = Brush.verticalGradient(listOf(lerp(bright, Color.White, 0.20f), bright, c), startY = 0f, endY = h),
            topLeft = Offset(x, 0f),
            size = Size(filled, h),
          )
        }
        x += sw
      }
    }

    // Dividers between sections.
    var dividerX = 0f
    for (i in 0 until segments.size - 1) {
      dividerX += w * (segments[i].millis.toFloat() / total)
      drawRect(Ink, topLeft = Offset(dividerX - 1.5.dp.toPx(), 0f), size = Size(3.dp.toPx(), h))
    }

    // Moving shimmer highlight (like the flames).
    val bandHalf = w * 0.20f
    val bandCenter = (shimmer.floatValue * 1.6f - 0.3f) * w
    clipPath(bar) {
      drawRect(
        brush =
          Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.30f), Color.Transparent),
            startX = bandCenter - bandHalf,
            endX = bandCenter + bandHalf,
          ),
        topLeft = Offset(0f, 0f),
        size = Size(w, h),
      )
    }

    // Animated playhead.
    drawLine(
      color = TextPrimary,
      start = Offset(playX, -2.dp.toPx()),
      end = Offset(playX, h + 2.dp.toPx()),
      strokeWidth = 3.dp.toPx(),
      cap = StrokeCap.Round,
    )
  }
}

private fun segmentColor(phase: TimerPhase): Color =
  when (phase) {
    TimerPhase.PREPARE -> PrepareBlue
    TimerPhase.REST -> RestGreen
    else -> WorkYellow
  }

/** Small parameter chip with a border in its phase colour; the active phase glows. */
@Composable
private fun ParamChip(
  label: String,
  value: String,
  accent: Color,
  modifier: Modifier = Modifier,
  active: Boolean = false,
) {
  val transition = rememberInfiniteTransition(label = "chip")
  val pulse by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
      label = "chipPulse",
    )

  val shape = RoundedCornerShape(10.dp)
  val glowAlpha = if (active) 0.45f + 0.55f * pulse else 0f
  val borderWidth = if (active) 3.dp else 1.dp
  val borderAlpha = if (active) 0.85f + 0.15f * pulse else 0.7f
  val scale = if (active) 1f + 0.03f * pulse else 1f

  Box(
    modifier =
      modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
  ) {
    if (active) {
      Box(
        modifier =
          Modifier
            .matchParentSize()
            .border(4.dp, accent.copy(alpha = glowAlpha), shape)
            .blur(13.dp),
      )
    }
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(Surface2.copy(alpha = if (active) 1f else 0.75f))
          .border(borderWidth, accent.copy(alpha = borderAlpha), shape)
          .padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(label, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
      Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun TimerDial(
  state: TimerState,
  color: Color,
  progress: Float,
  size: Dp,
) {
  Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val stroke = this.size.minDimension * 0.058f
      val inset = stroke / 2f
      val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
      val topLeft = Offset(inset, inset)
      drawArc(
        color = Surface2,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
      )
      drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
      )

      // Half-way marker on the ring (180 degrees from the top).
      val radius = (this.size.minDimension - stroke) / 2f
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val halfAngle = Math.toRadians(90.0) // -90° (top) + 180° sweep
      val dirX = kotlin.math.cos(halfAngle).toFloat()
      val dirY = kotlin.math.sin(halfAngle).toFloat()
      drawLine(
        color = TextPrimary.copy(alpha = 0.9f),
        start = Offset(cx + dirX * (radius - stroke * 0.55f), cy + dirY * (radius - stroke * 0.55f)),
        end = Offset(cx + dirX * (radius + stroke * 0.6f), cy + dirY * (radius + stroke * 0.6f)),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
    Text(
      text = formatMillis(state.remainingMillis),
      color = color,
      fontSize = (size.value * 0.28f).sp,
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
    OutlinedButton(
      onClick = { AppGraph.timer.reset() },
      modifier = Modifier.width(92.dp),
      contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
      Text("Reset", maxLines = 1, softWrap = false)
    }
    val primaryLabel =
      when {
        state.running -> "PAUSE"
        state.phase == TimerPhase.FINISHED -> "AGAIN"
        state.phase == TimerPhase.IDLE -> "START"
        else -> "RESUME"
      }
    Button(
      onClick = { AppGraph.timer.toggle() },
      modifier = Modifier.weight(1f).height(56.dp),
      contentPadding = PaddingValues(horizontal = 8.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text(
        primaryLabel,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        maxLines = 1,
        softWrap = false,
      )
    }
    OutlinedButton(
      onClick = { AppGraph.timer.skip() },
      modifier = Modifier.width(92.dp),
      contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
      Text("Skip", maxLines = 1, softWrap = false)
    }
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
 * Natural-looking fire drawn with an AGSL fragment shader (Android 13+). On
 * older devices it falls back to the procedural flame shapes.
 */
@Composable
private fun FlameBackground(
  color: Color,
  speed: Float,
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    FireShaderBackground(color, speed)
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
  float3 p3 = fract(float3(p.x, p.y, p.x) * 0.1031);
  p3 += dot(p3, p3.yzx + 33.33);
  return fract((p3.x + p3.y) * p3.z);
}

float noise(float2 p) {
  float2 i = floor(p);
  float2 f = fract(p);
  float a = hash21(i);
  float b = hash21(i + float2(1.0, 0.0));
  float c = hash21(i + float2(0.0, 1.0));
  float d = hash21(i + float2(1.0, 1.0));
  float2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);   // quintic (fewer axis artifacts)
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

  float2 p = float2(uv.x * aspect * 2.4, uv.y * 3.0);
  p.y -= uTime * 1.2;                       // flames rise
  // Turbulence in both axes so the pattern never forms horizontal bands.
  float wx = noise(p * 0.6 + float2(uTime * 0.30, 0.0));
  float wy = noise(p * 0.6 + float2(0.0, uTime * 0.45) + 31.0);
  float n = fbm(p + float2((wx - 0.5) * 1.7, (wy - 0.5) * 1.3));

  // Fire hugs the bottom edge and fades out towards the top.
  float heightMask = smoothstep(0.9, 0.05, uv.y);
  float fire = pow(n * heightMask, 1.7) * 1.5;

  float t = clamp(fire, 0.0, 1.0);
  float3 col = uColor * t * 1.2;
  col += float3(1.0, 0.86, 0.55) * pow(t, 3.0) * 0.5;   // hot core
  col += float3(0.025, 0.023, 0.02);                     // faint ambient

  return half4(col, 1.0);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun FireShaderBackground(
  color: Color,
  speed: Float,
) {
  // Accumulate flame time by integrating the current speed, so changing the pace
  // (e.g. entering a work round) never makes the flames jump.
  val fireTime = remember { mutableFloatStateOf(0f) }
  val currentSpeed = rememberUpdatedState(speed)
  LaunchedEffect(Unit) {
    var last = 0L
    while (true) {
      withFrameNanos { now ->
        if (last != 0L) {
          fireTime.floatValue += ((now - last) / 1_000_000_000f) * currentSpeed.value
        }
        last = now
      }
    }
  }

  val shader = remember { RuntimeShader(FIRE_AGSL) }
  val r = color.red
  val g = color.green
  val b = color.blue

  Canvas(modifier = Modifier.fillMaxSize()) {
    shader.setFloatUniform("uResolution", size.width, size.height)
    shader.setFloatUniform("uTime", fireTime.floatValue)
    shader.setFloatUniform("uColorR", r)
    shader.setFloatUniform("uColorG", g)
    shader.setFloatUniform("uColorB", b)
    drawRect(brush = ShaderBrush(shader), size = size)
  }
}

/**
 * Animated flame tongues rising from the bottom of the screen, tinted with the
 * phase colour. Purely procedural: flickering vertical-gradient ovals combined
 * with additive blending so overlapping flames glow.
 */
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
          cubicTo(
            x - flameWidth * 0.45f,
            h - flameHeight * 0.35f,
            x - flameWidth * 0.2f + lean * 0.5f,
            top + flameHeight * 0.3f,
            x + lean,
            top,
          )
          cubicTo(
            x + flameWidth * 0.2f + lean * 0.5f,
            top + flameHeight * 0.3f,
            x + flameWidth * 0.45f,
            h - flameHeight * 0.35f,
            x + flameWidth / 2f,
            h,
          )
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
