package com.example.wattwait.data.repository

import com.example.wattwait.data.local.database.dao.RateScheduleDao
import com.example.wattwait.data.local.database.entity.RateScheduleEntity
import com.example.wattwait.data.remote.api.OpenEiApi
import com.example.wattwait.data.remote.dto.EnergyRateTier
import com.example.wattwait.data.remote.dto.RateItem
import com.example.wattwait.domain.model.RateSchedule
import com.example.wattwait.domain.model.RateTier
import com.example.wattwait.domain.repository.IRateRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RateRepository @Inject constructor(
    private val openEiApi: OpenEiApi,
    private val rateScheduleDao: RateScheduleDao,
    private val gson: Gson,
    @Named("openei_api_key") private val apiKey: String
) : IRateRepository {

    companion object {
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    override suspend fun fetchRatesForZipCode(zipCode: String): Result<List<RateSchedule>> {
        return try {
            val response = openEiApi.getUtilityRatesByAddress(
                apiKey = apiKey,
                address = zipCode
            )

            if (response.isSuccessful) {
                val rateItems = response.body()?.items ?: emptyList()
                val rateSchedules = rateItems.mapNotNull { item ->
                    item.toDomain(zipCode = zipCode)
                }

                // Cache the results
                saveRateSchedules(rateSchedules)

                Result.success(rateSchedules)
            } else {
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchRatesForCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<List<RateSchedule>> {
        return try {
            val response = openEiApi.getUtilityRatesByCoordinates(
                apiKey = apiKey,
                latitude = latitude,
                longitude = longitude
            )

            if (response.isSuccessful) {
                val rateItems = response.body()?.items ?: emptyList()
                val rateSchedules = rateItems.mapNotNull { item ->
                    item.toDomain(latitude = latitude, longitude = longitude)
                }

                // Cache the results
                saveRateSchedules(rateSchedules)

                Result.success(rateSchedules)
            } else {
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCachedRates(): Flow<List<RateSchedule>> {
        return rateScheduleDao.getAllRateSchedules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCachedRatesForZipCode(zipCode: String): Flow<List<RateSchedule>> {
        return rateScheduleDao.getRateSchedulesByZipCode(zipCode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveRateSchedule(rateSchedule: RateSchedule) {
        rateScheduleDao.insertRateSchedule(rateSchedule.toEntity())
    }

    override suspend fun saveRateSchedules(rateSchedules: List<RateSchedule>) {
        rateScheduleDao.insertRateSchedules(rateSchedules.map { it.toEntity() })
    }

    override suspend fun getRateScheduleByLabel(label: String): RateSchedule? {
        return rateScheduleDao.getRateScheduleByLabel(label)?.toDomain()
    }

    override suspend fun isRateCacheValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return rateScheduleDao.countValidSchedules(currentTime) > 0
    }

    override suspend fun cleanupExpiredRates() {
        rateScheduleDao.deleteExpiredSchedules(System.currentTimeMillis())
    }

    private fun RateItem.toDomain(
        zipCode: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): RateSchedule? {
        val label = this.label ?: return null
        val currentTime = System.currentTimeMillis()

        return RateSchedule(
            label = label,
            utilityName = utility ?: "Unknown Utility",
            rateName = name ?: "Unknown Rate",
            zipCode = zipCode,
            latitude = latitude,
            longitude = longitude,
            energyRateStructure = energyRateStructure?.map { tiers ->
                tiers.map { tier -> tier.toDomain() }
            } ?: emptyList(),
            energyWeekdaySchedule = energyWeekdaySchedule ?: createDefaultSchedule(),
            energyWeekendSchedule = energyWeekendSchedule ?: createDefaultSchedule(),
            fixedMonthlyCharge = fixedMonthlyCharge,
            lastUpdated = currentTime,
            expiresAt = currentTime + CACHE_DURATION_MS
        )
    }

    private fun EnergyRateTier.toDomain(): RateTier {
        return RateTier(
            max = max,
            rate = rate ?: 0.0,
            adjustment = adjustment,
            unit = unit ?: "kWh"
        )
    }

    private fun createDefaultSchedule(): List<List<Int>> {
        // Default to single period (index 0) for all hours and months
        return List(12) { List(24) { 0 } }
    }

    private fun RateSchedule.toEntity(): RateScheduleEntity {
        return RateScheduleEntity(
            label = label,
            utilityName = utilityName,
            rateName = rateName,
            zipCode = zipCode,
            latitude = latitude,
            longitude = longitude,
            energyRateStructureJson = gson.toJson(energyRateStructure),
            energyWeekdayScheduleJson = gson.toJson(energyWeekdaySchedule),
            energyWeekendScheduleJson = gson.toJson(energyWeekendSchedule),
            fixedMonthlyCharge = fixedMonthlyCharge,
            lastUpdated = lastUpdated,
            expiresAt = expiresAt
        )
    }

    private fun RateScheduleEntity.toDomain(): RateSchedule {
        val rateStructureType = object : TypeToken<List<List<RateTier>>>() {}.type
        val scheduleType = object : TypeToken<List<List<Int>>>() {}.type

        return RateSchedule(
            label = label,
            utilityName = utilityName,
            rateName = rateName,
            zipCode = zipCode,
            latitude = latitude,
            longitude = longitude,
            energyRateStructure = gson.fromJson(energyRateStructureJson, rateStructureType) ?: emptyList(),
            energyWeekdaySchedule = gson.fromJson(energyWeekdayScheduleJson, scheduleType) ?: createDefaultSchedule(),
            energyWeekendSchedule = gson.fromJson(energyWeekendScheduleJson, scheduleType) ?: createDefaultSchedule(),
            fixedMonthlyCharge = fixedMonthlyCharge,
            lastUpdated = lastUpdated,
            expiresAt = expiresAt
        )
    }
}
