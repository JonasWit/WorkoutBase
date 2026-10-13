package com.wtk_apps.workoutbase.ui.trainings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wtk_apps.workoutbase.AppGraph
import com.wtk_apps.workoutbase.data.MAX_PRESETS
import com.wtk_apps.workoutbase.data.TrainingPreset
import com.wtk_apps.workoutbase.theme.AccentGreen
import com.wtk_apps.workoutbase.theme.Surface1
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.timer.TimerController
import com.wtk_apps.workoutbase.timer.formatMillis
import kotlinx.coroutines.launch

@Composable
fun TrainingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  val presets by AppGraph.settings.presets.collectAsStateWithLifecycle(initialValue = emptyList())
  var showDialog by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf("") }
  var limitReached by remember { mutableStateOf(false) }

  Column(
    modifier = modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text("Trainings", color = TextPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
      Spacer(Modifier.weight(1f))
      TextButton(onClick = onBack) { Text("Back", color = TextSecondary) }
    }

    Spacer(Modifier.height(8.dp))
    Text("${presets.size} / $MAX_PRESETS saved", color = TextSecondary, fontSize = 13.sp)

    Spacer(Modifier.height(16.dp))
    Button(
      onClick = {
        nameInput = "Workout ${presets.size + 1}"
        showDialog = true
      },
      enabled = presets.size < MAX_PRESETS,
      modifier = Modifier.fillMaxWidth().height(52.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text("SAVE CURRENT WORKOUT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }

    if (limitReached) {
      Spacer(Modifier.height(8.dp))
      Text("Limit of $MAX_PRESETS trainings reached. Delete one first.", color = TextSecondary, fontSize = 13.sp)
    }

    Spacer(Modifier.height(16.dp))
    if (presets.isEmpty()) {
      Text("No saved trainings yet. Configure the timer, then save it here.", color = TextSecondary)
    }
    presets.forEachIndexed { index, preset ->
      PresetRow(
        preset = preset,
        onLoad = {
          scope.launch {
            AppGraph.settings.save(preset.config)
            TimerController.configure(preset.config)
          }
          onBack()
        },
        onDelete = { scope.launch { AppGraph.settings.deletePreset(index) } },
      )
      Spacer(Modifier.height(10.dp))
    }
  }

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text("Save training") },
      text = {
        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          singleLine = true,
          label = { Text("Name") },
        )
      },
      confirmButton = {
        TextButton(onClick = {
          val name = nameInput.trim().ifBlank { "Workout ${presets.size + 1}" }
          scope.launch {
            val added = AppGraph.settings.addPreset(TrainingPreset(name, TimerController.state.value.config))
            limitReached = !added
          }
          showDialog = false
        }) {
          Text("Save")
        }
      },
      dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun PresetRow(preset: TrainingPreset, onLoad: () -> Unit, onDelete: () -> Unit) {
  val config = preset.config
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(Surface1, RoundedCornerShape(14.dp))
        .clickable(onClick = onLoad)
        .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(preset.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
      Spacer(Modifier.height(2.dp))
      Text(
        "${config.rounds} × ${formatMillis(config.workMillis)} work / ${formatMillis(config.restMillis)} rest",
        color = TextSecondary,
        fontSize = 13.sp,
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
      Text("LOAD", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
      TextButton(onClick = onDelete) { Text("✕", color = TextSecondary) }
    }
  }
}
