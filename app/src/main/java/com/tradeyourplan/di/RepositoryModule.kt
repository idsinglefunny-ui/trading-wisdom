// app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt
package com.tradeyourplan.di

import com.tradeyourplan.data.repository.AlarmRepository
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideQuoteRepository(repository: QuoteRepository): QuoteRepository = repository

    @Provides
    @Singleton
    fun provideAlarmRepository(repository: AlarmRepository): AlarmRepository = repository

    @Provides
    @Singleton
    fun provideSettingsRepository(repository: SettingsRepository): SettingsRepository = repository
}
