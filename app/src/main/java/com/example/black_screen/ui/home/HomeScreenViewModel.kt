package com.example.black_screen.ui.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.black_screen.service.BlackScreenService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the HomeScreen, managing permission checks
 * and reflecting the current running state of the BlackScreenService.
 */
class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission

    // UI state containing all statuses
    val uiState: StateFlow<HomeUiState> = combine(
        BlackScreenService.isServiceRunning,
        BlackScreenService.isOverlayShowing,
        _hasNotificationPermission,
        _hasOverlayPermission
    ) { serviceRunning, overlayShowing, notifGranted, overlayGranted ->
        HomeUiState(
            isServiceRunning = serviceRunning,
            isOverlayShowing = overlayShowing,
            hasNotificationPermission = notifGranted,
            hasOverlayPermission = overlayGranted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        checkPermissions()
    }

    /**
     * Re-checks the state of notification and overlay permissions on the device.
     */
    fun checkPermissions() {
        _hasNotificationPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
    }

    /**
     * Start the foreground service.
     */
    fun startService() {
        val intent = Intent(context, BlackScreenService::class.java).apply {
            action = BlackScreenService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    /**
     * Stop the foreground service.
     */
    fun stopService() {
        val intent = Intent(context, BlackScreenService::class.java).apply {
            action = BlackScreenService.ACTION_STOP
        }
        context.stopService(intent)
    }

    /**
     * Launch the overlay window from the UI.
     */
    fun showOverlay() {
        val intent = Intent(context, BlackScreenService::class.java).apply {
            action = BlackScreenService.ACTION_SHOW_OVERLAY
        }
        context.startService(intent)
    }
}

/**
 * Data class representing the UI state for the Home Screen.
 */
data class HomeUiState(
    val isServiceRunning: Boolean = false,
    val isOverlayShowing: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false
)
