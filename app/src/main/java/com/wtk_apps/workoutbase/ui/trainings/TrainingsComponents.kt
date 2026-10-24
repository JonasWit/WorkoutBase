package com.wtk_apps.workoutbase.ui.trainings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wtk_apps.workoutbase.R
import com.wtk_apps.workoutbase.data.TrainingPreset
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.PrepareBlue
import com.wtk_apps.workoutbase.theme.RestGreen
import com.wtk_apps.workoutbase.theme.Surface1
import com.wtk_apps.workoutbase.theme.Surface2
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import com.wtk_apps.workoutbase.theme.WorkYellow
import com.wtk_apps.workoutbase.timer.formatMillis
import com.wtk_apps.workoutbase.timer.totalSessionMillis

@Composable
internal fun DialogAction(
  label: String,
  filled: Boolean,
  accent: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  if (filled) {
    Button(
      onClick = onClick,
      modifier = modifier.height(50.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      modifier = modifier.height(50.dp),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.5.dp, accent.copy(alpha = 0.85f)),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
    ) {
      Text(label, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
  }
}

@Composable
internal fun PresetRow(
  preset: TrainingPreset,
  onClick: () -> Unit,
) {
  val config = preset.config
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Surface1, RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 14.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(preset.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
      Column(horizontalAlignment = Alignment.End) {
        Text(
          stringResource(R.string.label_total),
          color = TextSecondary,
          fontSize = 8.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = 1.sp,
        )
        Text(
          formatMillis(totalSessionMillis(config)),
          color = AccentGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
    Spacer(Modifier.height(9.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      DetailChip("ROUNDS", config.rounds.toString(), AccentGold, Modifier.weight(1f))
      DetailChip("WORK", formatMillis(config.workMillis), WorkYellow, Modifier.weight(1f))
      DetailChip("REST", formatMillis(config.restMillis), RestGreen, Modifier.weight(1f))
      DetailChip("PREP", formatMillis(config.prepareMillis), PrepareBlue, Modifier.weight(1f))
    }
  }
}

/** Small structured chip with a border in its phase colour. */
@Composable
internal fun DetailChip(
  label: String,
  value: String,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .background(Surface2)
        .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
        .padding(horizontal = 6.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(label, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}
