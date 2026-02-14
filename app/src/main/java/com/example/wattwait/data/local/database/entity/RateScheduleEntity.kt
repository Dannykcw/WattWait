package com.example.wattwait.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_schedules")
data class RateScheduleEntity(
    @PrimaryKey
    val label: String,
    val utilityName: String,
    val rateName: String,
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val energyRateStructureJson: String,
    val energyWeekdayScheduleJson: String,
    val energyWeekendScheduleJson: String,
    val fixedMonthlyCharge: Double?,
    val lastUpdated: Long,
    val expiresAt: Long
)
