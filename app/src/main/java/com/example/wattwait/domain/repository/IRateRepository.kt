package com.example.wattwait.domain.repository

import com.example.wattwait.domain.model.RateSchedule
import kotlinx.coroutines.flow.Flow

interface IRateRepository {

    suspend fun fetchRatesForZipCode(zipCode: String): Result<List<RateSchedule>>

    suspend fun fetchRatesForCoordinates(latitude: Double, longitude: Double): Result<List<RateSchedule>>

    fun getCachedRates(): Flow<List<RateSchedule>>

    fun getCachedRatesForZipCode(zipCode: String): Flow<List<RateSchedule>>

    suspend fun saveRateSchedule(rateSchedule: RateSchedule)

    suspend fun saveRateSchedules(rateSchedules: List<RateSchedule>)

    suspend fun getRateScheduleByLabel(label: String): RateSchedule?

    suspend fun isRateCacheValid(): Boolean

    suspend fun cleanupExpiredRates()
}
