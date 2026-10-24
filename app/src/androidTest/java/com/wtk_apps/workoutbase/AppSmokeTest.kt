package com.wtk_apps.workoutbase

import android.Manifest
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

/**
 * End-to-end smoke checks on a device: the app starts, the timer screen renders its text,
 * and navigation reaches the settings and trainings screens.
 *
 * Assertions use UiAutomator rather than Compose finders because the flame animation never
 * goes idle, which makes every Compose finder time out. The notification permission is
 * granted through a shell command because MIUI rejects UiAutomator grants.
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
  @get:Rule val notificationPermission: TestRule = ShellGrantRule(Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var scenario: ActivityScenario<MainActivity>
  private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

  @Before
  fun setUp() {
    scenario = ActivityScenario.launch(MainActivity::class.java)
    assertNotNull("timer screen did not appear", device.wait(Until.hasObject(By.text("WORKOUT BASE")), 10_000))
  }

  @After
  fun tearDown() {
    scenario.close()
  }

  @Test
  fun timerScreen_showsTitleAndNavigation() {
    assertNotNull(device.findObject(By.text("Trainings")))
    assertNotNull(device.findObject(By.text("Settings")))
  }

  @Test
  fun timerStartsInReadyPhase() {
    assertNotNull(device.findObject(By.text("READY")))
  }

  @Test
  fun settingsLink_opensTheSettingsScreen() {
    device.findObject(By.text("Settings")).click()
    assertNotNull(device.wait(Until.hasObject(By.text("SAVE")), 5_000))
  }

  @Test
  fun trainingsLink_opensTheTrainingsScreen() {
    device.findObject(By.text("Trainings")).click()
    assertNotNull(device.wait(Until.hasObject(By.text("SAVE CURRENT WORKOUT")), 5_000))
    assertNotNull("backup actions missing", device.findObject(By.text("EXPORT")))
  }
}

/** Grants a runtime permission from the shell before the activity launches. */
private class ShellGrantRule(
  private val permission: String,
) : TestRule {
  override fun apply(
    base: Statement,
    description: Description,
  ): Statement =
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
