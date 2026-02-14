package com.example.wattwait.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_mappings")
data class AppMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val applianceType: String,
    val efficiencyCategory: String,
    val isEnabled: Boolean = true
)
