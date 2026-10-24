package com.wtk_apps.workoutbase

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke checks on a real device: the app starts, the timer screen renders its
 * text, and navigation reaches the settings screen. These guard the resource move and the
 * screen split, which unit tests cannot see.
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
  @get:Rule(order = 0)
  val notificationPermission: GrantPermissionRule =
    GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

  @get:Rule(order = 1)
  val compose = createAndroidComposeRule<MainActivity>()

  @Test
  fun timerScreen_showsTitleAndNavigation() {
    compose.onNodeWithText("WORKOUT BASE").assertIsDisplayed()
    compose.onNodeWithText("Trainings").assertIsDisplayed()
    compose.onNodeWithText("Settings").assertIsDisplayed()
  }

  @Test
  fun timerStartsInReadyPhase() {
    compose.onNodeWithText("READY").assertIsDisplayed()
  }

  @Test
  fun settingsLink_opensTheSettingsScreen() {
    compose.onNodeWithText("Settings").performClick()
    compose.onNodeWithText("SAVE").assertIsDisplayed()
  }

  @Test
  fun trainingsLink_opensTheTrainingsScreen() {
    compose.onNodeWithText("Trainings").performClick()
    compose.onNodeWithText("SAVE CURRENT WORKOUT").assertIsDisplayed()
  }
}
