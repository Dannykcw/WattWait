package com.example.wattwait.domain.usecase

import com.example.wattwait.domain.model.RateSchedule
import com.example.wattwait.domain.repository.IRateRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import com.example.wattwait.util.DebugSettings
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class GetCurrentRateUseCase @Inject constructor(
    private val rateRepository: IRateRepository,
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val debugSettings: DebugSettings
) {
    data class RateInfo(
        val currentRate: Double,
        val isPeakTime: Boolean,
        val rateSchedule: RateSchedule
    )

    suspend operator fun invoke(): Result<RateInfo> {
        val selectedLabel = userPreferencesRepository.getSelectedRateLabel().first()
            ?: return Result.failure(Exception("No rate schedule selected"))

        val rateSchedule = rateRepository.getRateScheduleByLabel(selectedLabel)
            ?: return Result.failure(Exception("Rate schedule not found"))

        val now = LocalDateTime.now()
        val currentRate = rateSchedule.getCurrentRate(now)

        // Check for debug overrides
        val isPeakTime = when {
            debugSettings.shouldForcePeak() -> true
            debugSettings.shouldForceOffPeak() -> false
            else -> rateSchedule.isPeakTime(now)
        }

        return Result.success(
            RateInfo(
                currentRate = currentRate,
                isPeakTime = isPeakTime,
                rateSchedule = rateSchedule
            )
        )
    }
}
