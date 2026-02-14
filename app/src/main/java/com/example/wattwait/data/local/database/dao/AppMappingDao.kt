package com.example.wattwait.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wattwait.data.local.database.entity.AppMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMappingDao {

    @Query("SELECT * FROM app_mappings ORDER BY appName ASC")
    fun getAllMappings(): Flow<List<AppMappingEntity>>

    @Query("SELECT * FROM app_mappings WHERE isEnabled = 1")
    fun getEnabledMappings(): Flow<List<AppMappingEntity>>

    @Query("SELECT * FROM app_mappings WHERE packageName = :packageName LIMIT 1")
    suspend fun getMappingByPackage(packageName: String): AppMappingEntity?

    @Query("SELECT * FROM app_mappings WHERE id = :id LIMIT 1")
    suspend fun getMappingById(id: Long): AppMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: AppMappingEntity): Long

    @Update
    suspend fun updateMapping(mapping: AppMappingEntity)

    @Delete
    suspend fun deleteMapping(mapping: AppMappingEntity)

    @Query("DELETE FROM app_mappings WHERE id = :id")
    suspend fun deleteMappingById(id: Long)

    @Query("UPDATE app_mappings SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleMapping(id: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM app_mappings WHERE packageName = :packageName")
    suspend fun countByPackage(packageName: String): Int
}
