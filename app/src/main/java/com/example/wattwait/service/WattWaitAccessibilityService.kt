package com.example.wattwait.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.wattwait.domain.model.AppMapping
import com.example.wattwait.domain.repository.IAppMappingRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import com.example.wattwait.domain.usecase.CalculateApplianceCostUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WattWaitAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var appMappingRepository: IAppMappingRepository

    @Inject
    lateinit var userPreferencesRepository: IUserPreferencesRepository

    @Inject
    lateinit var calculateCostUseCase: CalculateApplianceCostUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var monitoredPackages: Map<String, AppMapping> = emptyMap()
    private var lastShownPackage: String? = null
    private var lastShownTime: Long = 0

    companion object {
        private const val OVERLAY_COOLDOWN_MS = 5000L // 5 seconds cooldown between overlays
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Start observing enabled app mappings
        serviceScope.launch {
            appMappingRepository.getEnabledMappings().collect { mappings ->
                monitoredPackages = mappings.associateBy { it.packageName }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Skip our own package
        if (packageName == this.packageName) return

        // Check if we should show overlay (cooldown check)
        val currentTime = System.currentTimeMillis()
        if (packageName == lastShownPackage &&
            currentTime - lastShownTime < OVERLAY_COOLDOWN_MS) {
            return
        }

        serviceScope.launch {
            // Check if service is enabled
            val isEnabled = userPreferencesRepository.isServiceEnabled().first()
            if (!isEnabled) return@launch

            // Check if this package is monitored
            val mapping = monitoredPackages[packageName] ?: return@launch

            // Calculate cost and show overlay
            showCostOverlay(mapping)
            lastShownPackage = packageName
            lastShownTime = currentTime
        }
    }

    private suspend fun showCostOverlay(mapping: AppMapping) {
        calculateCostUseCase(
            mapping.applianceType,
            mapping.efficiencyCategory
        ).onSuccess { calculation ->
            // Start overlay service with calculation data
            val intent = Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_APP_NAME, mapping.appName)
                putExtra(OverlayService.EXTRA_APPLIANCE_NAME, mapping.applianceType.displayName)
                putExtra(OverlayService.EXTRA_ESTIMATED_COST, calculation.estimatedCost)
                putExtra(OverlayService.EXTRA_CURRENT_RATE, calculation.currentRate)
                putExtra(OverlayService.EXTRA_IS_PEAK_TIME, calculation.isPeakTime)
                putExtra(OverlayService.EXTRA_OFF_PEAK_TIME, calculation.offPeakStartTime?.toString())
                putExtra(OverlayService.EXTRA_HOURS_UNTIL_OFF_PEAK, calculation.hoursUntilOffPeak ?: -1)
                putExtra(OverlayService.EXTRA_SAVINGS_AMOUNT, calculation.potentialSavings ?: 0.0)
                putExtra(OverlayService.EXTRA_SAVINGS_PERCENTAGE, calculation.savingsPercentage ?: 0.0)
                putExtra(OverlayService.EXTRA_ENVIRONMENTAL_MESSAGE, calculation.environmentalMessage)
            }
            startService(intent)
        }
    }

    override fun onInterrupt() {
        // Stop any active overlay
        stopService(Intent(this, OverlayService::class.java))
    }

    override fun onDestroy() {
        serviceScope.cancel()
        stopService(Intent(this, OverlayService::class.java))
        super.onDestroy()
    }
}
