package com.example.black_screen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.black_screen.MainActivity
import com.example.black_screen.theme.BlackScreenTheme
import com.example.black_screen.ui.overlay.BlackScreenOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground Service responsible for displaying the full-screen black overlay
 * and displaying a persistent status bar notification.
 */
class BlackScreenService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val currentTimeFlow = MutableStateFlow(sdf.format(Date()))

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Runs every minute
        serviceScope.launch {
            while (true) {
                currentTimeFlow.value = sdf.format(Date())
                delay(1000)
            }
        }

        _isServiceRunning.value = true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START -> {
                startForegroundServiceCompat()
            }

            ACTION_STOP -> {
                stopSelf()
            }

            ACTION_SHOW_OVERLAY -> {
                showOverlay()
            }

            ACTION_HIDE_OVERLAY -> {
                hideOverlay()
            }

            ACTION_TOGGLE_OVERLAY -> {
                if (_isOverlayShowing.value) {
                    hideOverlay()
                } else {
                    showOverlay()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceCompat() {
        val channelId = "black_screen_service_channel"
        val channelName = "Black Screen FGS Channel"

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the screen simulation service running active."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        // Action intents for Notification Buttons
        val toggleIntent = Intent(this, BlackScreenService::class.java).apply { action = ACTION_TOGGLE_OVERLAY }
        val togglePendingIntent = PendingIntent.getService(this, 1, toggleIntent, flag)

        val stopIntent = Intent(this, BlackScreenService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, flag)

        val mainActivityIntent = Intent(this, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, flag)

        val overlayStateText = if (_isOverlayShowing.value) "Overlay Active" else "Overlay Idle"

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Black Screen Utility")
            .setContentText("Service running: $overlayStateText")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(mainActivityPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_view, "Toggle Overlay", togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Service", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showOverlay() {
        if (_isOverlayShowing.value) return

        // Verify overlay drawing permissions
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BlackScreenService)
            setViewTreeSavedStateRegistryOwner(this@BlackScreenService)
            setContent {
                BlackScreenTheme {
                    val time by currentTimeFlow.collectAsState()
                    BlackScreenOverlay(
                        currentTime = time,
                        onDismiss = { hideOverlay() }
                    )
                }
            }
        }

        try {
            windowManager?.addView(view, params)
            overlayView = view
            _isOverlayShowing.value = true
            startForegroundServiceCompat() // Update notification text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        if (!_isOverlayShowing.value) return
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
        _isOverlayShowing.value = false
        startForegroundServiceCompat() // Update notification text
    }

    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        _isServiceRunning.value = false
        _isOverlayShowing.value = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 8847

        const val ACTION_START = "com.example.black_screen.action.START"
        const val ACTION_STOP = "com.example.black_screen.action.STOP"
        const val ACTION_SHOW_OVERLAY = "com.example.black_screen.action.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.black_screen.action.HIDE_OVERLAY"
        const val ACTION_TOGGLE_OVERLAY = "com.example.black_screen.action.TOGGLE_OVERLAY"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private val _isOverlayShowing = MutableStateFlow(false)
        val isOverlayShowing = _isOverlayShowing.asStateFlow()
    }
}
