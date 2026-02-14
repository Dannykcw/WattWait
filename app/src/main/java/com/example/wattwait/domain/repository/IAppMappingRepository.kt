package com.example.wattwait.domain.repository

import com.example.wattwait.domain.model.AppMapping
import kotlinx.coroutines.flow.Flow

interface IAppMappingRepository {

    fun getAllMappings(): Flow<List<AppMapping>>

    fun getEnabledMappings(): Flow<List<AppMapping>>

    suspend fun getMappingByPackage(packageName: String): AppMapping?

    suspend fun getMappingById(id: Long): AppMapping?

    suspend fun addMapping(mapping: AppMapping): Long

    suspend fun updateMapping(mapping: AppMapping)

    suspend fun deleteMapping(id: Long)

    suspend fun toggleMapping(id: Long, enabled: Boolean)

    suspend fun isMappingExists(packageName: String): Boolean
}
