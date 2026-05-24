package com.tradeyourplan.di

import com.tradeyourplan.data.api.QuoteApiService
import com.tradeyourplan.data.api.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideQuoteApiService(): QuoteApiService {
        return RetrofitClient.quoteApiService
    }
}
