package com.example.wattwait.domain.model

enum class EfficiencyCategory(
    val displayName: String,
    val multiplier: Double,
    val description: String
) {
    EFFICIENT(
        displayName = "Energy Efficient",
        multiplier = 0.7,
        description = "Modern, energy-efficient model (uses ~30% less energy)"
    ),
    NORMAL(
        displayName = "Standard",
        multiplier = 1.0,
        description = "Average energy consumption"
    ),
    INEFFICIENT(
        displayName = "Older/Inefficient",
        multiplier = 1.3,
        description = "Older model or less efficient (uses ~30% more energy)"
    );

    companion object {
        fun fromString(value: String): EfficiencyCategory {
            return entries.find { it.name == value } ?: NORMAL
        }
    }
}
