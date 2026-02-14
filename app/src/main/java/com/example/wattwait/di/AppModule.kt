package com.example.wattwait.di

import android.content.Context
import com.example.wattwait.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("openei_api_key")
    fun provideOpenEiApiKey(): String {
        return BuildConfig.OPENEI_API_KEY
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    @Named("application_context")
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
}
