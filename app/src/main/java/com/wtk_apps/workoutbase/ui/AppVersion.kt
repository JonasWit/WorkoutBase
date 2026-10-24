package com.wtk_apps.workoutbase.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wtk_apps.workoutbase.BuildConfig
import com.wtk_apps.workoutbase.R
import com.wtk_apps.workoutbase.theme.TextSecondary

/**
 * Version shown under the app title. It comes from the build, so the number on screen is
 * the number of the installed build and never drifts from the manifest.
 */
@Composable
fun AppVersion(modifier: Modifier = Modifier) {
  Text(
    text = stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
    color = TextSecondary,
    fontSize = 11.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 1.sp,
    modifier = modifier,
  )
}
