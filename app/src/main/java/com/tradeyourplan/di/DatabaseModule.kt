// app/src/main/java/com/tradeyourplan/di/DatabaseModule.kt
package com.tradeyourplan.di

import android.content.Context
import androidx.room.Room
import com.tradeyourplan.data.local.MIGRATION_1_2
import com.tradeyourplan.data.local.TradeYourPlanDatabase
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TradeYourPlanDatabase {
        return Room.databaseBuilder(
            context,
            TradeYourPlanDatabase::class.java,
            "tradeyourplan.db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: TradeYourPlanDatabase) = database.quoteDao()

    @Provides
    @Singleton
    fun provideAlarmDao(database: TradeYourPlanDatabase) = database.alarmDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: TradeYourPlanDatabase) = database.settingsDao()
}
