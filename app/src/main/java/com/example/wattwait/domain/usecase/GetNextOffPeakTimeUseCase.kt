package com.example.wattwait.domain.usecase

import com.example.wattwait.domain.model.RateSchedule
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class GetNextOffPeakTimeUseCase @Inject constructor() {

    data class OffPeakInfo(
        val startTime: LocalTime,
        val rate: Double,
        val hoursUntil: Int
    )

    operator fun invoke(rateSchedule: RateSchedule): OffPeakInfo? {
        val now = LocalDateTime.now()
        val avgRate = rateSchedule.calculateAverageRate()

        // Look ahead up to 24 hours to find next off-peak time
        for (hoursAhead in 1..24) {
            val futureTime = now.plusHours(hoursAhead.toLong())
            val futureRate = rateSchedule.getCurrentRate(futureTime)

            // Off-peak is when rate is below average
            if (futureRate < avgRate) {
                return OffPeakInfo(
                    startTime = futureTime.toLocalTime().withMinute(0).withSecond(0),
                    rate = futureRate,
                    hoursUntil = hoursAhead
                )
            }
        }

        // If no off-peak found in next 24 hours, return lowest rate time
        return findLowestRateTime(rateSchedule, now)
    }

    private fun findLowestRateTime(
        rateSchedule: RateSchedule,
        startTime: LocalDateTime
    ): OffPeakInfo? {
        var lowestRate = Double.MAX_VALUE
        var lowestRateHour = -1
        var hoursUntilLowest = -1

        for (hoursAhead in 1..24) {
            val futureTime = startTime.plusHours(hoursAhead.toLong())
            val rate = rateSchedule.getCurrentRate(futureTime)

            if (rate < lowestRate) {
                lowestRate = rate
                lowestRateHour = futureTime.hour
                hoursUntilLowest = hoursAhead
            }
        }

        return if (lowestRateHour >= 0) {
            OffPeakInfo(
                startTime = LocalTime.of(lowestRateHour, 0),
                rate = lowestRate,
                hoursUntil = hoursUntilLowest
            )
        } else {
            null
        }
    }
}
