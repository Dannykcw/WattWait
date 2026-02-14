package com.example.wattwait.domain.usecase

import com.example.wattwait.domain.model.ApplianceType
import com.example.wattwait.domain.model.CostCalculation
import com.example.wattwait.domain.model.EfficiencyCategory
import javax.inject.Inject

class CalculateApplianceCostUseCase @Inject constructor(
    private val getCurrentRateUseCase: GetCurrentRateUseCase,
    private val getNextOffPeakTimeUseCase: GetNextOffPeakTimeUseCase
) {
    suspend operator fun invoke(
        applianceType: ApplianceType,
        efficiencyCategory: EfficiencyCategory
    ): Result<CostCalculation> {
        val rateInfoResult = getCurrentRateUseCase()

        return rateInfoResult.map { rateInfo ->
            val baseKwh = applianceType.averageKwhPerUse
            val adjustedKwh = baseKwh * efficiencyCategory.multiplier
            val estimatedCost = adjustedKwh * rateInfo.currentRate

            val offPeakInfo = if (rateInfo.isPeakTime) {
                getNextOffPeakTimeUseCase(rateInfo.rateSchedule)
            } else null

            val potentialSavings = if (offPeakInfo != null) {
                val offPeakCost = adjustedKwh * offPeakInfo.rate
                estimatedCost - offPeakCost
            } else null

            val savingsPercentage = if (potentialSavings != null && estimatedCost > 0) {
                (potentialSavings / estimatedCost) * 100
            } else null

            CostCalculation(
                applianceType = applianceType,
                efficiencyCategory = efficiencyCategory,
                currentRate = rateInfo.currentRate,
                estimatedKwh = adjustedKwh,
                estimatedCost = estimatedCost,
                isPeakTime = rateInfo.isPeakTime,
                offPeakRate = offPeakInfo?.rate,
                offPeakStartTime = offPeakInfo?.startTime,
                hoursUntilOffPeak = offPeakInfo?.hoursUntil,
                potentialSavings = potentialSavings,
                savingsPercentage = savingsPercentage,
                environmentalMessage = CostCalculation.generateEnvironmentalMessage(rateInfo.isPeakTime)
            )
        }
    }
}
