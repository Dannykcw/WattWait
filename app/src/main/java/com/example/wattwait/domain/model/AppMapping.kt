package com.example.wattwait.domain.model

data class AppMapping(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val applianceType: ApplianceType,
    val efficiencyCategory: EfficiencyCategory,
    val isEnabled: Boolean = true
) {
    val estimatedKwhPerUse: Double
        get() = applianceType.averageKwhPerUse * efficiencyCategory.multiplier
}
