package com.example.wattwait.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isServiceEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val selectedRateLabel: String?
)
