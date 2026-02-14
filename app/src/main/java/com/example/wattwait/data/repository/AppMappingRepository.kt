package com.example.wattwait.data.repository

import com.example.wattwait.data.local.database.dao.AppMappingDao
import com.example.wattwait.data.local.database.entity.AppMappingEntity
import com.example.wattwait.domain.model.AppMapping
import com.example.wattwait.domain.model.ApplianceType
import com.example.wattwait.domain.model.EfficiencyCategory
import com.example.wattwait.domain.repository.IAppMappingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppMappingRepository @Inject constructor(
    private val appMappingDao: AppMappingDao
) : IAppMappingRepository {

    override fun getAllMappings(): Flow<List<AppMapping>> {
        return appMappingDao.getAllMappings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEnabledMappings(): Flow<List<AppMapping>> {
        return appMappingDao.getEnabledMappings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMappingByPackage(packageName: String): AppMapping? {
        return appMappingDao.getMappingByPackage(packageName)?.toDomain()
    }

    override suspend fun getMappingById(id: Long): AppMapping? {
        return appMappingDao.getMappingById(id)?.toDomain()
    }

    override suspend fun addMapping(mapping: AppMapping): Long {
        return appMappingDao.insertMapping(mapping.toEntity())
    }

    override suspend fun updateMapping(mapping: AppMapping) {
        appMappingDao.updateMapping(mapping.toEntity())
    }

    override suspend fun deleteMapping(id: Long) {
        appMappingDao.deleteMappingById(id)
    }

    override suspend fun toggleMapping(id: Long, enabled: Boolean) {
        appMappingDao.toggleMapping(id, enabled)
    }

    override suspend fun isMappingExists(packageName: String): Boolean {
        return appMappingDao.countByPackage(packageName) > 0
    }

    private fun AppMappingEntity.toDomain(): AppMapping {
        return AppMapping(
            id = id,
            packageName = packageName,
            appName = appName,
            applianceType = ApplianceType.fromString(applianceType),
            efficiencyCategory = EfficiencyCategory.fromString(efficiencyCategory),
            isEnabled = isEnabled
        )
    }

    private fun AppMapping.toEntity(): AppMappingEntity {
        return AppMappingEntity(
            id = id,
            packageName = packageName,
            appName = appName,
            applianceType = applianceType.name,
            efficiencyCategory = efficiencyCategory.name,
            isEnabled = isEnabled
        )
    }
}
