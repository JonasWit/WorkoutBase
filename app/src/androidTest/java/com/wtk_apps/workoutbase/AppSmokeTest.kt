package com.wtk_apps.workoutbase

import android.Manifest
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

/**
 * End-to-end smoke checks on a real device: the app starts, the timer screen renders its
 * text, and navigation reaches the settings and trainings screens.
 *
 * The notification permission is granted through a shell command instead of
 * GrantPermissionRule, because MIUI rejects UiAutomation grants; without it the system
 * dialog covers the UI under test.
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

  @get:Rule(order = 0) val notificationPermission: TestRule = ShellGrantRule(Manifest.permission.POST_NOTIFICATIONS)

  @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

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

/** Grants a runtime permission from the shell before the activity launches. */
private class ShellGrantRule(private val permission: String) : TestRule {

  override fun apply(base: Statement, description: Description): Statement =
    object : Statement() {
      override fun evaluate() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        val descriptor =
          instrumentation.uiAutomation.executeShellCommand(
            "pm grant $packageName $permission",
          )
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        base.evaluate()
      }
    }
}
