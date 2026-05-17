package com.doruruma.black_screen.ui.home

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doruruma.black_screen.service.BlackScreenService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Unit Test for [HomeScreenViewModel], verifying its state integration
 * with the FGS Companion flow states.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenViewModelTest {

    private lateinit var viewModel: HomeScreenViewModel

    @Before
    fun setup() {
        viewModel = HomeScreenViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun viewModel_initialization_setsUpStateFlows() = runBlocking {
        // Assert viewModel and its primary state flow are initialized correctly
        assertNotNull(viewModel.uiState)
        
        val currentState = viewModel.uiState.first()
        // We assert that the reactive properties map correctly to the initial state
        assertEquals(BlackScreenService.isServiceRunning.value, currentState.isServiceRunning)
        assertEquals(BlackScreenService.isOverlayShowing.value, currentState.isOverlayShowing)
    }

    @Test
    fun permissionChecking_executesWithoutCrashing() {
        // Assert checkPermissions runs seamlessly without throwing android SDK stub exceptions
        viewModel.checkPermissions()
        assertNotNull(viewModel.hasOverlayPermission.value)
        assertNotNull(viewModel.hasNotificationPermission.value)
    }
}
