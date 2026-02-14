package com.example.wattwait.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime

data class RateSchedule(
    val label: String,
    val utilityName: String,
    val rateName: String,
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val energyRateStructure: List<List<RateTier>>,
    val energyWeekdaySchedule: List<List<Int>>,
    val energyWeekendSchedule: List<List<Int>>,
    val fixedMonthlyCharge: Double?,
    val lastUpdated: Long,
    val expiresAt: Long
) {
    fun getCurrentPeriodIndex(dateTime: LocalDateTime = LocalDateTime.now()): Int {
        val month = dateTime.monthValue - 1
        val hour = dateTime.hour
        val isWeekend = dateTime.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        val schedule = if (isWeekend) energyWeekendSchedule else energyWeekdaySchedule

        return if (month in schedule.indices && hour in schedule[month].indices) {
            schedule[month][hour]
        } else {
            0
        }
    }

    fun getCurrentRate(dateTime: LocalDateTime = LocalDateTime.now()): Double {
        val periodIndex = getCurrentPeriodIndex(dateTime)
        return if (periodIndex in energyRateStructure.indices) {
            energyRateStructure[periodIndex].firstOrNull()?.rate ?: 0.0
        } else {
            0.0
        }
    }

    fun isPeakTime(dateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        val currentRate = getCurrentRate(dateTime)
        val avgRate = calculateAverageRate()
        return currentRate > avgRate
    }

    fun calculateAverageRate(): Double {
        val allRates = energyRateStructure.mapNotNull { tiers ->
            tiers.firstOrNull()?.rate
        }
        return if (allRates.isNotEmpty()) allRates.average() else 0.0
    }

    fun getLowestRate(): Double {
        return energyRateStructure.mapNotNull { tiers ->
            tiers.firstOrNull()?.rate
        }.minOrNull() ?: 0.0
    }

    fun getHighestRate(): Double {
        return energyRateStructure.mapNotNull { tiers ->
            tiers.firstOrNull()?.rate
        }.maxOrNull() ?: 0.0
    }
}

data class RateTier(
    val max: Double?,
    val rate: Double,
    val adjustment: Double? = null,
    val unit: String? = "kWh"
)
