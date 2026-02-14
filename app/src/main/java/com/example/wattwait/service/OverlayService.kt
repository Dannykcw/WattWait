package com.example.wattwait.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.wattwait.ui.overlay.CostOverlayContent
import com.example.wattwait.ui.theme.WattWaitTheme
import java.time.LocalTime

class OverlayService : Service() {

    companion object {
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_APPLIANCE_NAME = "appliance_name"
        const val EXTRA_ESTIMATED_COST = "estimated_cost"
        const val EXTRA_CURRENT_RATE = "current_rate"
        const val EXTRA_IS_PEAK_TIME = "is_peak_time"
        const val EXTRA_OFF_PEAK_TIME = "off_peak_time"
        const val EXTRA_HOURS_UNTIL_OFF_PEAK = "hours_until_off_peak"
        const val EXTRA_SAVINGS_AMOUNT = "savings_amount"
        const val EXTRA_SAVINGS_PERCENTAGE = "savings_percentage"
        const val EXTRA_ENVIRONMENTAL_MESSAGE = "environmental_message"

        private const val OVERLAY_DISPLAY_TIME_MS = 8000L
    }

    private var overlayView: ComposeView? = null
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    private val lifecycleOwner = OverlayLifecycleOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { showOverlay(it) }
        return START_NOT_STICKY
    }

    private fun showOverlay(intent: Intent) {
        // Dismiss any existing overlay
        dismissOverlay()

        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: return
        val applianceName = intent.getStringExtra(EXTRA_APPLIANCE_NAME) ?: return
        val estimatedCost = intent.getDoubleExtra(EXTRA_ESTIMATED_COST, 0.0)
        val currentRate = intent.getDoubleExtra(EXTRA_CURRENT_RATE, 0.0)
        val isPeakTime = intent.getBooleanExtra(EXTRA_IS_PEAK_TIME, false)
        val offPeakTimeStr = intent.getStringExtra(EXTRA_OFF_PEAK_TIME)
        val hoursUntilOffPeak = intent.getIntExtra(EXTRA_HOURS_UNTIL_OFF_PEAK, -1)
        val savingsAmount = intent.getDoubleExtra(EXTRA_SAVINGS_AMOUNT, 0.0)
        val savingsPercentage = intent.getDoubleExtra(EXTRA_SAVINGS_PERCENTAGE, 0.0)
        val environmentalMessage = intent.getStringExtra(EXTRA_ENVIRONMENTAL_MESSAGE) ?: ""

        val offPeakTime = offPeakTimeStr?.let {
            try {
                LocalTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                WattWaitTheme {
                    CostOverlayContent(
                        appName = appName,
                        applianceName = applianceName,
                        estimatedCost = estimatedCost,
                        currentRate = currentRate,
                        isPeakTime = isPeakTime,
                        offPeakTime = offPeakTime,
                        hoursUntilOffPeak = if (hoursUntilOffPeak >= 0) hoursUntilOffPeak else null,
                        savingsAmount = savingsAmount,
                        savingsPercentage = savingsPercentage,
                        environmentalMessage = environmentalMessage,
                        onDismiss = { dismissOverlay() }
                    )
                }
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100
        }

        windowManager.addView(overlayView, layoutParams)

        // Auto-dismiss after timeout
        dismissRunnable = Runnable { dismissOverlay() }
        handler.postDelayed(dismissRunnable!!, OVERLAY_DISPLAY_TIME_MS)
    }

    private fun dismissOverlay() {
        dismissRunnable?.let { handler.removeCallbacks(it) }
        dismissRunnable = null

        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View may already be removed
            }
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        overlayView = null
    }

    override fun onDestroy() {
        dismissOverlay()
        super.onDestroy()
    }

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        init {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}
