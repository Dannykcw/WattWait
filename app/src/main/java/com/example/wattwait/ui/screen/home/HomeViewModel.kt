package com.example.wattwait.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wattwait.domain.repository.IAppMappingRepository
import com.example.wattwait.domain.repository.IRateRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import com.example.wattwait.domain.usecase.GetCurrentRateUseCase
import com.example.wattwait.domain.usecase.GetNextOffPeakTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val appMappingRepository: IAppMappingRepository,
    private val rateRepository: IRateRepository,
    private val getCurrentRateUseCase: GetCurrentRateUseCase,
    private val getNextOffPeakTimeUseCase: GetNextOffPeakTimeUseCase
) : ViewModel() {

    data class UiState(
        val isServiceEnabled: Boolean = false,
        val currentRate: Double? = null,
        val isPeakTime: Boolean = false,
        val nextOffPeakTime: String? = null,
        val hoursUntilOffPeak: Int? = null,
        val monitoredAppCount: Int = 0,
        val hasLocation: Boolean = false,
        val hasSelectedRate: Boolean = false,
        val environmentalTip: String = "Enable the service to start saving energy and money!",
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userPreferencesRepository.initializePreferences()
        }

        viewModelScope.launch {
            combine(
                userPreferencesRepository.isServiceEnabled(),
                appMappingRepository.getEnabledMappings(),
                userPreferencesRepository.getUserLocation(),
                userPreferencesRepository.getSelectedRateLabel()
            ) { isEnabled, mappings, location, rateLabel ->
                _uiState.update { state ->
                    state.copy(
                        isServiceEnabled = isEnabled,
                        monitoredAppCount = mappings.size,
                        hasLocation = location?.isValid == true,
                        hasSelectedRate = !rateLabel.isNullOrBlank(),
                        isLoading = false
                    )
                }
            }.collect { }
        }

        viewModelScope.launch {
            loadRateInfo()
        }
    }

    private suspend fun loadRateInfo() {
        getCurrentRateUseCase().onSuccess { rateInfo ->
            val offPeakInfo = if (rateInfo.isPeakTime) {
                getNextOffPeakTimeUseCase(rateInfo.rateSchedule)
            } else null

            val tip = if (rateInfo.isPeakTime) {
                "It's peak hours! Power plants are working harder. Consider waiting for off-peak to save money and reduce emissions."
            } else {
                "Great timing! Off-peak hours mean cleaner energy and lower costs. Perfect time to run your appliances!"
            }

            _uiState.update { state ->
                state.copy(
                    currentRate = rateInfo.currentRate,
                    isPeakTime = rateInfo.isPeakTime,
                    nextOffPeakTime = offPeakInfo?.startTime?.format(
                        DateTimeFormatter.ofPattern("h:mm a")
                    ),
                    hoursUntilOffPeak = offPeakInfo?.hoursUntil,
                    environmentalTip = tip,
                    error = null
                )
            }
        }.onFailure { error ->
            _uiState.update { state ->
                state.copy(
                    error = if (state.hasSelectedRate) error.message else null
                )
            }
        }
    }

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setServiceEnabled(enabled)
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadRateInfo()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
