package com.example.wattwait.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug settings for testing overlay behavior without waiting for actual peak hours.
 * These settings are NOT persisted and reset when the app restarts.
 */
@Singleton
class DebugSettings @Inject constructor() {

    enum class PeakTimeOverride {
        AUTO,       // Use actual time-based calculation
        FORCE_PEAK, // Always show as peak time
        FORCE_OFF_PEAK // Always show as off-peak
    }

    private val _peakTimeOverride = MutableStateFlow(PeakTimeOverride.AUTO)
    val peakTimeOverride: StateFlow<PeakTimeOverride> = _peakTimeOverride.asStateFlow()

    private val _debugModeEnabled = MutableStateFlow(false)
    val debugModeEnabled: StateFlow<Boolean> = _debugModeEnabled.asStateFlow()

    fun setDebugModeEnabled(enabled: Boolean) {
        _debugModeEnabled.value = enabled
        if (!enabled) {
            _peakTimeOverride.value = PeakTimeOverride.AUTO
        }
    }

    fun setPeakTimeOverride(override: PeakTimeOverride) {
        _peakTimeOverride.value = override
    }

    fun shouldForcePeak(): Boolean {
        return _debugModeEnabled.value && _peakTimeOverride.value == PeakTimeOverride.FORCE_PEAK
    }

    fun shouldForceOffPeak(): Boolean {
        return _debugModeEnabled.value && _peakTimeOverride.value == PeakTimeOverride.FORCE_OFF_PEAK
    }
}
