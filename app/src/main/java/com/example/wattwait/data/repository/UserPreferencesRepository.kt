package com.example.wattwait.data.repository

import com.example.wattwait.data.local.database.dao.UserPreferencesDao
import com.example.wattwait.data.local.database.entity.UserPreferencesEntity
import com.example.wattwait.domain.model.UserLocation
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) : IUserPreferencesRepository {

    override fun isServiceEnabled(): Flow<Boolean> {
        return userPreferencesDao.getUserPreferences().map { prefs ->
            prefs?.isServiceEnabled ?: false
        }
    }

    override suspend fun setServiceEnabled(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.setServiceEnabled(enabled)
    }

    override fun getUserLocation(): Flow<UserLocation?> {
        return userPreferencesDao.getUserPreferences().map { prefs ->
            prefs?.toUserLocation()
        }
    }

    override suspend fun setUserLocation(location: UserLocation) {
        ensurePreferencesExist()
        userPreferencesDao.updateLocation(
            zipCode = location.zipCode,
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    override fun getSelectedRateLabel(): Flow<String?> {
        return userPreferencesDao.getUserPreferences().map { prefs ->
            prefs?.selectedRateLabel
        }
    }

    override suspend fun setSelectedRateLabel(label: String?) {
        ensurePreferencesExist()
        userPreferencesDao.setSelectedRate(label)
    }

    override fun hasCompletedOnboarding(): Flow<Boolean> {
        return userPreferencesDao.getUserPreferences().map { prefs ->
            prefs?.hasCompletedOnboarding ?: false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.setOnboardingCompleted(completed)
    }

    override suspend fun initializePreferences() {
        ensurePreferencesExist()
    }

    private suspend fun ensurePreferencesExist() {
        val existing = userPreferencesDao.getUserPreferencesOnce()
        if (existing == null) {
            userPreferencesDao.insertUserPreferences(
                UserPreferencesEntity(
                    id = 1,
                    zipCode = null,
                    latitude = null,
                    longitude = null,
                    isServiceEnabled = false,
                    hasCompletedOnboarding = false,
                    selectedRateLabel = null
                )
            )
        }
    }

    private fun UserPreferencesEntity.toUserLocation(): UserLocation? {
        return if (zipCode != null || (latitude != null && longitude != null)) {
            UserLocation(
                zipCode = zipCode,
                latitude = latitude,
                longitude = longitude,
                isGpsLocation = latitude != null && longitude != null && zipCode == null
            )
        } else {
            null
        }
    }
}
