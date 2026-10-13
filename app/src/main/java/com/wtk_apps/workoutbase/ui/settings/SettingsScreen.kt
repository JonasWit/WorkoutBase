package com.wtk_apps.workoutbase.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wtk_apps.workoutbase.AppGraph
import com.wtk_apps.workoutbase.theme.AccentGreen
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.timer.TimerConfig
import com.wtk_apps.workoutbase.timer.TimerController
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  var config by remember { mutableStateOf(TimerController.state.value.config) }

  Column(
    modifier = modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text("Settings", color = TextPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
      Spacer(Modifier.weight(1f))
      TextButton(onClick = onBack) { Text("Back", color = TextSecondary) }
    }

    Spacer(Modifier.height(16.dp))

    SectionTitle("Session")
    Stepper("Rounds", config.rounds.toString()) {
      config = config.copy(rounds = (config.rounds + it).coerceIn(1, 99))
    }
    Stepper("Work (s)", (config.workMillis / 1000).toString()) {
      config = config.copy(workMillis = ((config.workMillis / 1000 + it * 15L).coerceIn(5L, 3600L)) * 1000)
    }
    Stepper("Rest (s)", (config.restMillis / 1000).toString()) {
      config = config.copy(restMillis = ((config.restMillis / 1000 + it * 15L).coerceIn(0L, 3600L)) * 1000)
    }
    Stepper("Prepare (s)", (config.prepareMillis / 1000).toString()) {
      config = config.copy(prepareMillis = ((config.prepareMillis / 1000 + it * 5L).coerceIn(0L, 600L)) * 1000)
    }

    Spacer(Modifier.height(16.dp))
    SectionTitle("Cues")
    Stepper("Warning (s)", (config.warnMillis / 1000).toString()) {
      config = config.copy(warnMillis = ((config.warnMillis / 1000 + it * 5L).coerceIn(0L, 600L)) * 1000)
    }
    Stepper("Countdown (s)", config.countdownSeconds.toString()) {
      config = config.copy(countdownSeconds = (config.countdownSeconds + it).coerceIn(0, 10))
    }
    Toggle("Half-way cue", config.halfWayEnabled) { config = config.copy(halfWayEnabled = it) }
    Toggle("Warning cue", config.warnEnabled) { config = config.copy(warnEnabled = it) }
    Toggle("Countdown cue", config.countdownEnabled) { config = config.copy(countdownEnabled = it) }
    Toggle("Sound", config.soundEnabled) { config = config.copy(soundEnabled = it) }
    Toggle("Vibration", config.vibrationEnabled) { config = config.copy(vibrationEnabled = it) }

    Spacer(Modifier.height(24.dp))
    Button(
      onClick = {
        scope.launch {
          AppGraph.settings.save(config)
          TimerController.configure(config)
          onBack()
        }
      },
      modifier = Modifier.fillMaxWidth().height(52.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text("SAVE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text.uppercase(),
    color = TextSecondary,
    fontSize = 12.sp,
    letterSpacing = 3.sp,
    fontWeight = FontWeight.Medium,
  )
  Spacer(Modifier.height(8.dp))
}

@Composable
private fun Stepper(label: String, value: String, onChange: (Int) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, color = TextPrimary)
    Row(verticalAlignment = Alignment.CenterVertically) {
      FilledTonalButton(onClick = { onChange(-1) }, modifier = Modifier.size(44.dp)) { Text("−") }
      Text(
        value,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      FilledTonalButton(onClick = { onChange(1) }, modifier = Modifier.size(44.dp)) { Text("+") }
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
