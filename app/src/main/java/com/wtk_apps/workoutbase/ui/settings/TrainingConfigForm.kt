package com.wtk_apps.workoutbase.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.Outline1
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.timer.TimerConfig
import com.wtk_apps.workoutbase.timer.MAX_PHASE_SECONDS

/**
 * The session/cues form, shared between the global Settings screen and the
 * per-training edit screen. It is a pure controlled component: it renders
 * [config] and reports edits through [onConfigChange].
 */
@Composable
internal fun TrainingConfigForm(
  config: TimerConfig,
  onConfigChange: (TimerConfig) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    SectionTitle("Session")
    NumberSetting("Rounds", config.rounds, step = 1, range = 1..99) {
      onConfigChange(config.copy(rounds = it))
    }
    NumberSetting("Work (s)", (config.workMillis / 1000).toInt(), step = 5, range = 5..MAX_PHASE_SECONDS) {
      onConfigChange(config.copy(workMillis = it * 1000L))
    }
    NumberSetting("Rest (s)", (config.restMillis / 1000).toInt(), step = 5, range = 0..MAX_PHASE_SECONDS) {
      onConfigChange(config.copy(restMillis = it * 1000L))
    }
    NumberSetting("Prepare (s)", (config.prepareMillis / 1000).toInt(), step = 5, range = 0..MAX_PHASE_SECONDS) {
      onConfigChange(config.copy(prepareMillis = it * 1000L))
    }

    Spacer(Modifier.height(16.dp))
    SectionTitle("Cues")
    NumberSetting("Countdown (s)", config.countdownSeconds, step = 1, range = 0..10) {
      onConfigChange(config.copy(countdownSeconds = it))
    }
    Toggle("Half-way cue", config.halfWayEnabled) { onConfigChange(config.copy(halfWayEnabled = it)) }
    Toggle("Countdown cue", config.countdownEnabled) { onConfigChange(config.copy(countdownEnabled = it)) }
    Toggle("Sound", config.soundEnabled) { onConfigChange(config.copy(soundEnabled = it)) }
    Toggle("Vibration", config.vibrationEnabled) { onConfigChange(config.copy(vibrationEnabled = it)) }
  }
}

@Composable
internal fun SectionTitle(text: String) {
  Text(
    text = text.uppercase(),
    color = TextSecondary,
    fontSize = 12.sp,
    letterSpacing = 3.sp,
    fontWeight = FontWeight.Medium,
  )
  Spacer(Modifier.height(8.dp))
}

/**
 * A numeric setting with -/+ buttons (stepping by [step]) and an editable text
 * field so a value can also be typed directly.
 */
@Composable
private fun NumberSetting(
  label: String,
  value: Int,
  step: Int,
  range: IntRange,
  onChange: (Int) -> Unit,
) {
  // When the user is typing, [draft] holds the raw text; otherwise the field
  // shows the canonical [value]. This keeps the field correct whether the value
  // changes via the keyboard or via the -/+ buttons.
  var draft by remember { mutableStateOf<String?>(null) }

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    Row(verticalAlignment = Alignment.CenterVertically) {
      StepButton(plus = false) {
        draft = null
        onChange((value - step).coerceIn(range))
      }
      OutlinedTextField(
        value = draft ?: value.toString(),
        onValueChange = { new ->
          val digits = new.filter { it.isDigit() }.take(4)
          val parsed = digits.toIntOrNull()
          when {
            // Still typing (e.g. "3" on the way to "30") — keep the draft.
            parsed == null || parsed < range.first -> draft = digits
            // Over the maximum: clamp immediately so it can never be exceeded.
            parsed > range.last -> {
              draft = range.last.toString()
              onChange(range.last)
            }
            else -> {
              draft = digits
              onChange(parsed)
            }
          }
        },
        singleLine = true,
        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        modifier =
          Modifier.width(78.dp).padding(horizontal = 8.dp).onFocusChanged { focusState ->
            if (!focusState.isFocused) draft = null
          },
      )
      StepButton(plus = true) {
        draft = null
        onChange((value + step).coerceIn(range))
      }
    }
  }
}

/** Round stepper button with a centred, drawn +/- glyph (no font-metric offsets). */
@Composable
private fun StepButton(plus: Boolean, onClick: () -> Unit) {
  Box(
    modifier =
      Modifier.size(44.dp)
        .clip(CircleShape)
        .background(Surface2)
        .border(1.dp, Outline1, CircleShape)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(20.dp)) {
      val stroke = 2.4.dp.toPx()
      val c = center
      val arm = size.minDimension / 2f * 0.82f
      drawLine(AccentGold, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), strokeWidth = stroke, cap = StrokeCap.Round)
      if (plus) {
        drawLine(AccentGold, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), strokeWidth = stroke, cap = StrokeCap.Round)
      }
    }
  }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    Switch(checked = checked, onCheckedChange = onChange)
  }
}
