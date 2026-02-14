package com.example.wattwait.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.wattwait.domain.model.AppMapping
import com.example.wattwait.domain.model.CostCalculation
import com.example.wattwait.domain.repository.IAppMappingRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import com.example.wattwait.domain.usecase.CalculateApplianceCostUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WattWaitAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "WattWaitAccessibility"
        private const val OVERLAY_COOLDOWN_MS = 5000L // 5 seconds cooldown between overlays
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityServiceEntryPoint {
        fun appMappingRepository(): IAppMappingRepository
        fun userPreferencesRepository(): IUserPreferencesRepository
        fun calculateCostUseCase(): CalculateApplianceCostUseCase
    }

    private lateinit var appMappingRepository: IAppMappingRepository
    private lateinit var userPreferencesRepository: IUserPreferencesRepository
    private lateinit var calculateCostUseCase: CalculateApplianceCostUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var monitoredPackages: Map<String, AppMapping> = emptyMap()
    private var lastShownPackage: String? = null
    private var lastShownTime: Long = 0
    private var isServiceReady = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")

        try {
            // Get dependencies using EntryPointAccessors
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            appMappingRepository = entryPoint.appMappingRepository()
            userPreferencesRepository = entryPoint.userPreferencesRepository()
            calculateCostUseCase = entryPoint.calculateCostUseCase()

            isServiceReady = true
            Log.d(TAG, "Dependencies injected successfully")

            // Start observing enabled app mappings
            serviceScope.launch {
                appMappingRepository.getEnabledMappings().collect { mappings ->
                    monitoredPackages = mappings.associateBy { it.packageName }
                    Log.d(TAG, "Monitoring ${mappings.size} apps: ${mappings.map { it.appName }}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize dependencies", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isServiceReady) {
            Log.w(TAG, "Service not ready, ignoring event")
            return
        }

        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Skip our own package and system UI
        if (packageName == this.packageName ||
            packageName == "com.android.systemui" ||
            packageName.startsWith("com.android.launcher")) {
            return
        }

        Log.d(TAG, "Window changed to: $packageName")

        // Check if we should show overlay (cooldown check)
        val currentTime = System.currentTimeMillis()
        if (packageName == lastShownPackage &&
            currentTime - lastShownTime < OVERLAY_COOLDOWN_MS) {
            Log.d(TAG, "Skipping due to cooldown")
            return
        }

        serviceScope.launch {
            try {
                // Check if service is enabled
                val isEnabled = userPreferencesRepository.isServiceEnabled().first()
                if (!isEnabled) {
                    Log.d(TAG, "Service is disabled in settings")
                    return@launch
                }

                // Check if this package is monitored
                val mapping = monitoredPackages[packageName]
                if (mapping == null) {
                    Log.d(TAG, "Package $packageName is not monitored")
                    return@launch
                }

                Log.d(TAG, "Showing overlay for ${mapping.appName}")

                // Calculate cost and show overlay
                showCostOverlay(mapping)
                lastShownPackage = packageName
                lastShownTime = currentTime
            } catch (e: Exception) {
                Log.e(TAG, "Error processing accessibility event", e)
            }
        }
    }

    private suspend fun showCostOverlay(mapping: AppMapping) {
        val result = calculateCostUseCase(
            mapping.applianceType,
            mapping.efficiencyCategory
        )

        result.onSuccess { calculation ->
            Log.d(TAG, "Cost calculated: $${calculation.estimatedCost}")
            startOverlayService(mapping, calculation)
        }.onFailure { error ->
            Log.w(TAG, "Cost calculation failed: ${error.message}, showing fallback overlay")
            // Show fallback overlay with default values
            showFallbackOverlay(mapping, error.message ?: "Rate data unavailable")
        }
    }

    private fun startOverlayService(mapping: AppMapping, calculation: CostCalculation) {
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
            // Enable blocking mode during peak hours
            putExtra(OverlayService.EXTRA_BLOCKING_MODE, calculation.isPeakTime)
        }
        startService(intent)
    }

    private fun showFallbackOverlay(mapping: AppMapping, errorMessage: String) {
        // Show overlay with fallback data when rate info is unavailable
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_APP_NAME, mapping.appName)
            putExtra(OverlayService.EXTRA_APPLIANCE_NAME, mapping.applianceType.displayName)
            putExtra(OverlayService.EXTRA_ESTIMATED_COST, -1.0) // Indicates unavailable
            putExtra(OverlayService.EXTRA_CURRENT_RATE, 0.0)
            putExtra(OverlayService.EXTRA_IS_PEAK_TIME, false)
            putExtra(OverlayService.EXTRA_HOURS_UNTIL_OFF_PEAK, -1)
            putExtra(OverlayService.EXTRA_SAVINGS_AMOUNT, 0.0)
            putExtra(OverlayService.EXTRA_SAVINGS_PERCENTAGE, 0.0)
            putExtra(OverlayService.EXTRA_ENVIRONMENTAL_MESSAGE,
                "Set your location in Settings to see electricity costs")
        }
        startService(intent)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
        stopService(Intent(this, OverlayService::class.java))
    }

    override fun onDestroy() {
        Log.d(TAG, "Accessibility service destroyed")
        serviceScope.cancel()
        stopService(Intent(this, OverlayService::class.java))
        super.onDestroy()
    }
}
