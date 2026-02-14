package com.example.wattwait.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wattwait.data.local.database.dao.AppMappingDao
import com.example.wattwait.data.local.database.dao.RateScheduleDao
import com.example.wattwait.data.local.database.dao.UserPreferencesDao
import com.example.wattwait.data.local.database.entity.AppMappingEntity
import com.example.wattwait.data.local.database.entity.RateScheduleEntity
import com.example.wattwait.data.local.database.entity.UserPreferencesEntity

@Database(
    entities = [
        AppMappingEntity::class,
        RateScheduleEntity::class,
        UserPreferencesEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WattWaitDatabase : RoomDatabase() {

    abstract fun appMappingDao(): AppMappingDao
    abstract fun rateScheduleDao(): RateScheduleDao
    abstract fun userPreferencesDao(): UserPreferencesDao
}
