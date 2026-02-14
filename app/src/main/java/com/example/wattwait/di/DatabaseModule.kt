package com.example.wattwait.di

import android.content.Context
import androidx.room.Room
import com.example.wattwait.data.local.database.WattWaitDatabase
import com.example.wattwait.data.local.database.dao.AppMappingDao
import com.example.wattwait.data.local.database.dao.RateScheduleDao
import com.example.wattwait.data.local.database.dao.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WattWaitDatabase {
        return Room.databaseBuilder(
            context,
            WattWaitDatabase::class.java,
            "wattwait_database"
        ).build()
    }

    @Provides
    fun provideAppMappingDao(database: WattWaitDatabase): AppMappingDao {
        return database.appMappingDao()
    }

    @Provides
    fun provideRateScheduleDao(database: WattWaitDatabase): RateScheduleDao {
        return database.rateScheduleDao()
    }

    @Provides
    fun provideUserPreferencesDao(database: WattWaitDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
}
