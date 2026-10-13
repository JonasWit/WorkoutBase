package com.wtk_apps.workoutbase.ui.trainings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wtk_apps.workoutbase.AppGraph
import com.wtk_apps.workoutbase.data.MAX_PRESETS
import com.wtk_apps.workoutbase.data.TrainingPreset
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.PrepareBlue
import com.wtk_apps.workoutbase.theme.RestGreen
import com.wtk_apps.workoutbase.theme.Surface1
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.theme.WorkRedOrange
import com.wtk_apps.workoutbase.theme.WorkYellow
import com.wtk_apps.workoutbase.timer.TimerController
import com.wtk_apps.workoutbase.timer.formatMillis
import kotlinx.coroutines.launch

@Composable
fun TrainingsScreen(onBack: () -> Unit, onEdit: (Int) -> Unit, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  val presets by AppGraph.settings.presets.collectAsStateWithLifecycle(initialValue = emptyList())
  var showSaveDialog by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf("") }
  var limitReached by remember { mutableStateOf(false) }
  var actionsFor by remember { mutableStateOf<Int?>(null) }

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
        showSaveDialog = true
      },
      enabled = presets.size < MAX_PRESETS,
      modifier = Modifier.fillMaxWidth().height(52.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = MaterialTheme.colorScheme.onPrimary),
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
      PresetRow(preset = preset, onClick = { actionsFor = index })
      Spacer(Modifier.height(10.dp))
    }
  }

  if (showSaveDialog) {
    AlertDialog(
      onDismissRequest = { showSaveDialog = false },
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
          showSaveDialog = false
        }) {
          Text("Save")
        }
      },
      dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") } },
    )
  }

  val actionIndex = actionsFor
  if (actionIndex != null) {
    val preset = presets.getOrNull(actionIndex)
    Dialog(onDismissRequest = { actionsFor = null }) {
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
            text = preset?.name ?: "Training",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.height(18.dp))
          DialogAction("LOAD", filled = true, accent = AccentGold) {
            preset?.let {
              scope.launch {
                AppGraph.settings.save(it.config)
                TimerController.configure(it.config)
              }
              onBack()
            }
            actionsFor = null
          }
          Spacer(Modifier.height(10.dp))
          DialogAction("EDIT", filled = false, accent = AccentGold) {
            actionsFor = null
            onEdit(actionIndex)
          }
          Spacer(Modifier.height(10.dp))
          DialogAction("DELETE", filled = false, accent = WorkRedOrange) {
            actionsFor = null
            scope.launch { AppGraph.settings.deletePreset(actionIndex) }
          }
          Spacer(Modifier.height(4.dp))
          TextButton(onClick = { actionsFor = null }) { Text("Cancel", color = TextSecondary) }
        }
      }
    }
  }
}

@Composable
private fun DialogAction(label: String, filled: Boolean, accent: Color, onClick: () -> Unit) {
  if (filled) {
    Button(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(50.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(50.dp),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.5.dp, accent.copy(alpha = 0.85f)),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetRow(preset: TrainingPreset, onClick: () -> Unit) {
  val config = preset.config
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(Surface1, RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(preset.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
      Spacer(Modifier.height(9.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        DetailChip("ROUNDS", config.rounds.toString(), AccentGold)
        DetailChip("WORK", formatMillis(config.workMillis), WorkYellow)
        DetailChip("REST", formatMillis(config.restMillis), RestGreen)
        if (config.prepareMillis > 0) {
          DetailChip("PREP", formatMillis(config.prepareMillis), PrepareBlue)
        }
      }
    }
    Spacer(Modifier.width(10.dp))
    Text("⋯", color = TextSecondary, fontSize = 22.sp)
  }
}

/** Small structured chip with a border in its phase colour. */
@Composable
private fun DetailChip(label: String, value: String, accent: Color) {
  Column(
    modifier =
      Modifier.clip(RoundedCornerShape(10.dp))
        .background(Surface2)
        .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp, vertical = 6.dp),
  ) {
    Text(label, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
  }
}
