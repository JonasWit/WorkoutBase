package com.wtk_apps.workoutbase.ui.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wtk_apps.workoutbase.theme.AccentGold
import com.wtk_apps.workoutbase.theme.TextPrimary
import com.wtk_apps.workoutbase.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun StopwatchScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  var elapsedMillis by remember { mutableLongStateOf(0L) }
  var running by remember { mutableStateOf(false) }

  LaunchedEffect(running) {
    if (running) {
      val base = System.nanoTime() - elapsedMillis * 1_000_000L
      while (isActive) {
        elapsedMillis = (System.nanoTime() - base) / 1_000_000L
        delay(33L)
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Stopwatch", color = TextPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onBack) { Text("Back", color = TextSecondary) }
      }
      Spacer(Modifier.weight(1f))
      Text(
        text = formatStopwatch(elapsedMillis),
        color = TextPrimary,
        fontSize = 64.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
      )
      Spacer(Modifier.weight(1f))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(onClick = { running = false; elapsedMillis = 0L }) { Text("Reset") }
        Button(
          onClick = { running = !running },
          modifier = Modifier.weight(1f).height(56.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
        ) {
          Text(
            if (running) "PAUSE" else "START",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
          )
        }
      }
    }
  }
}

/** Formats elapsed milliseconds as `MM:SS.cc` (centiseconds). */
fun formatStopwatch(millis: Long): String {
  val total = millis.coerceAtLeast(0L)
  val minutes = total / 60_000L
  val seconds = (total % 60_000L) / 1_000L
  val centis = (total % 1_000L) / 10L
  return "%02d:%02d.%02d".format(minutes, seconds, centis)
}
