package com.example.wattwait.domain.repository

import com.example.wattwait.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {

    fun isServiceEnabled(): Flow<Boolean>

    suspend fun setServiceEnabled(enabled: Boolean)

    fun getUserLocation(): Flow<UserLocation?>

    suspend fun setUserLocation(location: UserLocation)

    fun getSelectedRateLabel(): Flow<String?>

    suspend fun setSelectedRateLabel(label: String?)

    fun hasCompletedOnboarding(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun initializePreferences()
}
