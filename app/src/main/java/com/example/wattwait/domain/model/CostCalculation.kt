package com.example.wattwait.domain.model

import java.time.LocalTime

data class CostCalculation(
    val applianceType: ApplianceType,
    val efficiencyCategory: EfficiencyCategory,
    val currentRate: Double,
    val estimatedKwh: Double,
    val estimatedCost: Double,
    val isPeakTime: Boolean,
    val offPeakRate: Double?,
    val offPeakStartTime: LocalTime?,
    val hoursUntilOffPeak: Int?,
    val potentialSavings: Double?,
    val savingsPercentage: Double?,
    val environmentalMessage: String
) {
    companion object {
        fun generateEnvironmentalMessage(isPeakTime: Boolean): String {
            return if (isPeakTime) {
                "During peak hours, power plants burn more fossil fuels to meet demand. " +
                "Waiting for off-peak hours reduces carbon emissions and saves money!"
            } else {
                "Great timing! Off-peak hours typically mean the grid uses more renewable energy. " +
                "You're saving money and helping the environment!"
            }
        }
    }
}
