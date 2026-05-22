// app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt
package com.tradeyourplan.di

import com.tradeyourplan.data.repository.*
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
    fun provideQuoteRepository(/* TODO */): QuoteRepository {
        // TODO: Implement in Task 5
        throw NotImplementedError()
    }
}
