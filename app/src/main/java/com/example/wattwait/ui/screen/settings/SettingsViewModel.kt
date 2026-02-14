package com.example.wattwait.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wattwait.domain.model.RateSchedule
import com.example.wattwait.domain.model.UserLocation
import com.example.wattwait.domain.repository.IRateRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val rateRepository: IRateRepository
) : ViewModel() {

    data class UiState(
        val zipCode: String = "",
        val isServiceEnabled: Boolean = false,
        val isAccessibilityEnabled: Boolean = false,
        val canDrawOverlays: Boolean = false,
        val availableRates: List<RateSchedule> = emptyList(),
        val selectedRateLabel: String? = null,
        val isLoading: Boolean = false,
        val isFetchingRates: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkPermissions()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferencesRepository.initializePreferences()

            userPreferencesRepository.getUserLocation().collect { location ->
                _uiState.update { state ->
                    state.copy(zipCode = location?.zipCode ?: "")
                }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.isServiceEnabled().collect { enabled ->
                _uiState.update { it.copy(isServiceEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.getSelectedRateLabel().collect { label ->
                _uiState.update { it.copy(selectedRateLabel = label) }
            }
        }

        viewModelScope.launch {
            rateRepository.getCachedRates().collect { rates ->
                _uiState.update { it.copy(availableRates = rates) }
            }
        }
    }

    fun checkPermissions() {
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val overlayPermission = Settings.canDrawOverlays(context)

        _uiState.update { state ->
            state.copy(
                isAccessibilityEnabled = accessibilityEnabled,
                canDrawOverlays = overlayPermission
            )
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${context.packageName}/.service.WattWaitAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.contains(serviceName) ||
                enabledServices.contains(context.packageName)
    }

    fun updateZipCode(zipCode: String) {
        _uiState.update { it.copy(zipCode = zipCode, error = null) }
    }

    fun saveLocation() {
        val zipCode = _uiState.value.zipCode.trim()
        if (zipCode.length != 5 || !zipCode.all { it.isDigit() }) {
            _uiState.update { it.copy(error = "Please enter a valid 5-digit ZIP code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userPreferencesRepository.setUserLocation(
                UserLocation(
                    zipCode = zipCode,
                    latitude = null,
                    longitude = null,
                    isGpsLocation = false
                )
            )

            _uiState.update { it.copy(isLoading = false, successMessage = "Location saved") }

            // Auto-fetch rates after saving location
            fetchRates()
        }
    }

    fun fetchRates() {
        val zipCode = _uiState.value.zipCode.trim()
        if (zipCode.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a ZIP code first") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingRates = true, error = null) }

            rateRepository.fetchRatesForZipCode(zipCode)
                .onSuccess { rates ->
                    _uiState.update { state ->
                        state.copy(
                            availableRates = rates,
                            isFetchingRates = false,
                            successMessage = "Found ${rates.size} rate${if (rates.size != 1) "s" else ""}"
                        )
                    }

                    // Auto-select first rate if none selected
                    if (_uiState.value.selectedRateLabel == null && rates.isNotEmpty()) {
                        selectRate(rates.first().label)
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isFetchingRates = false,
                            error = "Failed to fetch rates: ${error.message}"
                        )
                    }
                }
        }
    }

    fun selectRate(label: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedRateLabel(label)
            _uiState.update { it.copy(successMessage = "Rate selected") }
        }
    }

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setServiceEnabled(enabled)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun getOverlaySettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
    }
}
