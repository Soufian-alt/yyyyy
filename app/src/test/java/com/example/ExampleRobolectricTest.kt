package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30]) // Verify Android 11 (Galaxy A03s target) compatibility
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesMyLiquidOS() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Liquid OS", appName)
    }

    @Test
    fun mainActivityRegistersHomeLauncherIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val activities = context.packageManager.queryIntentActivities(homeIntent, 0)
        assertTrue(
            "MainActivity should resolve CATEGORY_HOME",
            activities.any { it.activityInfo.packageName == context.packageName }
        )
    }

    @Test
    fun launcherCoreFlowsOpenAndCloseCleanly() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_dock").assertIsDisplayed()

        // Open and close App Drawer
        composeTestRule.onNodeWithTag("dock_app_drawer_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_drawer_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_drawer_btn").performClick()
        composeTestRule.waitForIdle()

        // Open and close Control Center
        composeTestRule.onNodeWithTag("open_control_center_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("control_center_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_control_center_btn").performClick()
        composeTestRule.waitForIdle()

        // Open and close Settings
        composeTestRule.onNodeWithTag("open_settings_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_back_btn").performClick()
        composeTestRule.waitForIdle()

        // Open and unlock Lock Screen Preview
        composeTestRule.onNodeWithTag("open_lock_preview_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("lock_preview_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("lock_preview_unlock_btn").performClick()
        composeTestRule.waitForIdle()
    }
}
