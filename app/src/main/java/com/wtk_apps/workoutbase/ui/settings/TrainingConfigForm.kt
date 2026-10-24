package com.wtk_apps.workoutbase.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.Outline1
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.timer.DurationParts
import com.wtk_apps.workoutbase.timer.TimerConfig
import com.wtk_apps.workoutbase.timer.durationParts
import com.wtk_apps.workoutbase.timer.millisFromParts

/**
 * The session/cues form, shared between the global Settings screen and the
 * per-training edit screen. It is a pure controlled component: it renders
 * [config] and reports edits through [onConfigChange].
 *
 * Time settings use separate minutes/seconds fields; tapping a field opens a
 * modal where the value can be typed.
 */
@Composable
internal fun TrainingConfigForm(
  config: TimerConfig,
  onConfigChange: (TimerConfig) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    SectionTitle("Session")

    SingleNumberField("Rounds", config.rounds, 1..99) {
      onConfigChange(config.copy(rounds = it))
    }

    val work = durationParts(config.workMillis)
    DurationFields(
      label = "Work",
      parts = work,
      minimumMillis = 1_000L,
      onChange = { onConfigChange(config.copy(workMillis = it)) },
    )

    val rest = durationParts(config.restMillis)
    DurationFields(
      label = "Rest",
      parts = rest,
      minimumMillis = 0L,
      onChange = { onConfigChange(config.copy(restMillis = it)) },
    )

    val prepare = durationParts(config.prepareMillis)
    DurationFields(
      label = "Prepare",
      parts = prepare,
      minimumMillis = 0L,
      onChange = { onConfigChange(config.copy(prepareMillis = it)) },
    )

    Spacer(Modifier.height(16.dp))
    SectionTitle("Cues")
    SingleNumberField("Countdown (s)", config.countdownSeconds, 0..10) {
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

/** A single number (no minutes/seconds split), edited through a modal. */
@Composable
private fun SingleNumberField(
  label: String,
  value: Int,
  range: IntRange,
  onChange: (Int) -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    ValueBox(value.toString(), minWidth = 72.dp) { showDialog = true }
  }

  if (showDialog) {
    NumberInputDialog(
      title = label,
      value = value,
      range = range,
      onConfirm = {
        onChange(it)
        showDialog = false
      },
      onDismiss = { showDialog = false },
    )
  }
}

/** Minutes + seconds fields, each edited through a modal. */
@Composable
private fun DurationFields(
  label: String,
  parts: DurationParts,
  minimumMillis: Long,
  onChange: (Long) -> Unit,
) {
  TimeField(
    label = label,
    minutes = parts.minutes,
    seconds = parts.seconds,
    onMinutes = { onChange(maxOf(millisFromParts(it, parts.seconds), minimumMillis)) },
    onSeconds = { onChange(maxOf(millisFromParts(parts.minutes, it), minimumMillis)) },
  )
}

@Composable
private fun TimeField(
  label: String,
  minutes: Int,
  seconds: Int,
  onMinutes: (Int) -> Unit,
  onSeconds: (Int) -> Unit,
) {
  var editMinutes by remember { mutableStateOf(false) }
  var editSeconds by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ValueBox("%02d".format(minutes), minWidth = 56.dp) { editMinutes = true }
      Text(":", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
      ValueBox("%02d".format(seconds), minWidth = 56.dp) { editSeconds = true }
    }
  }

  if (editMinutes) {
    NumberInputDialog(
      title = "$label — minutes",
      value = minutes,
      range = 0..99,
      onConfirm = {
        onMinutes(it)
        editMinutes = false
      },
      onDismiss = { editMinutes = false },
    )
  }
  if (editSeconds) {
    NumberInputDialog(
      title = "$label — seconds",
      value = seconds,
      range = 0..59,
      onConfirm = {
        onSeconds(it)
        editSeconds = false
      },
      onDismiss = { editSeconds = false },
    )
  }
}

/** Tappable display box that shows a value and opens the editor. */
@Composable
private fun ValueBox(
  text: String,
  minWidth: androidx.compose.ui.unit.Dp,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .height(44.dp)
        .defaultMinSize(minWidth = minWidth)
        .clip(RoundedCornerShape(10.dp))
        .background(Surface2)
        .border(1.dp, Outline1, RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun NumberInputDialog(
  title: String,
  value: Int,
  range: IntRange,
  onConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  var text by remember { mutableStateOf(value.toString()) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
          value = text,
          onValueChange = { new -> text = new.filter { it.isDigit() }.take(3) },
          singleLine = true,
          textStyle = TextStyle(color = TextPrimary, fontSize = 22.sp, textAlign = TextAlign.Center),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
          modifier = Modifier.width(140.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          FormDialogButton("CANCEL", filled = false, accent = TextSecondary, modifier = Modifier.weight(1f), onClick = onDismiss)
          FormDialogButton("OK", filled = true, accent = AccentGold, modifier = Modifier.weight(1f)) {
            onConfirm((text.toIntOrNull() ?: value).coerceIn(range))
          }
        }
      }
    }
  }
}

@Composable
private fun FormDialogButton(
  label: String,
  filled: Boolean,
  accent: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  if (filled) {
    Button(
      onClick = onClick,
      modifier = modifier.height(48.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      modifier = modifier.height(48.dp),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.5.dp, accent.copy(alpha = 0.85f)),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  }
}

@Composable
private fun Toggle(
  label: String,
  checked: Boolean,
  onChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    Switch(checked = checked, onCheckedChange = onChange)
  }
}
