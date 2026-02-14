package com.example.wattwait.di

import com.example.wattwait.data.repository.AppMappingRepository
import com.example.wattwait.data.repository.RateRepository
import com.example.wattwait.data.repository.UserPreferencesRepository
import com.example.wattwait.domain.repository.IAppMappingRepository
import com.example.wattwait.domain.repository.IRateRepository
import com.example.wattwait.domain.repository.IUserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppMappingRepository(
        impl: AppMappingRepository
    ): IAppMappingRepository

    @Binds
    @Singleton
    abstract fun bindRateRepository(
        impl: RateRepository
    ): IRateRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepository
    ): IUserPreferencesRepository
}
