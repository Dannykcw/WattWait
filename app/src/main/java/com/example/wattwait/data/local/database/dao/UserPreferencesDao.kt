package com.example.wattwait.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wattwait.data.local.database.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferencesOnce(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

    @Update
    suspend fun updateUserPreferences(preferences: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET isServiceEnabled = :enabled WHERE id = 1")
    suspend fun setServiceEnabled(enabled: Boolean)

    @Query("UPDATE user_preferences SET zipCode = :zipCode, latitude = :latitude, longitude = :longitude WHERE id = 1")
    suspend fun updateLocation(zipCode: String?, latitude: Double?, longitude: Double?)

    @Query("UPDATE user_preferences SET selectedRateLabel = :label WHERE id = 1")
    suspend fun setSelectedRate(label: String?)

    @Query("UPDATE user_preferences SET hasCompletedOnboarding = :completed WHERE id = 1")
    suspend fun setOnboardingCompleted(completed: Boolean)
}
