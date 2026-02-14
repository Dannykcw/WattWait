package com.example.wattwait.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wattwait.data.local.database.entity.RateScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateScheduleDao {

    @Query("SELECT * FROM rate_schedules")
    fun getAllRateSchedules(): Flow<List<RateScheduleEntity>>

    @Query("SELECT * FROM rate_schedules WHERE label = :label LIMIT 1")
    suspend fun getRateScheduleByLabel(label: String): RateScheduleEntity?

    @Query("SELECT * FROM rate_schedules WHERE zipCode = :zipCode")
    fun getRateSchedulesByZipCode(zipCode: String): Flow<List<RateScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateSchedule(rateSchedule: RateScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateSchedules(rateSchedules: List<RateScheduleEntity>)

    @Query("DELETE FROM rate_schedules WHERE label = :label")
    suspend fun deleteRateSchedule(label: String)

    @Query("DELETE FROM rate_schedules WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredSchedules(currentTime: Long)

    @Query("SELECT COUNT(*) FROM rate_schedules WHERE expiresAt > :currentTime")
    suspend fun countValidSchedules(currentTime: Long): Int
}
