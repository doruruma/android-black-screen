package com.doruruma.black_screen.ui.overlay

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the [BlackScreenOverlay] composable.
 */
class BlackScreenOverlayTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun overlay_displaysCurrentTimeAndButton() {
        val testTime = "12:34:56"
        var dismissClicked = false

        composeTestRule.setContent {
            BlackScreenOverlay(
                currentTime = testTime,
                onDismiss = { dismissClicked = true }
            )
        }

        // Verify that the time is displayed
        composeTestRule.onNodeWithText(testTime).assertExists()

        // Verify that the Hide Overlay button exists and click it
        composeTestRule.onNodeWithText("Hide Overlay").assertExists().performClick()

        // Assert that the dismiss callback was triggered
        assertTrue(dismissClicked)
    }
}
