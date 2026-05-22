// app/src/main/java/com/tradeyourplan/data/local/TradeYourPlanDatabase.kt
package com.tradeyourplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradeyourplan.data.local.dao.AlarmDao
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.dao.SettingsDao
import com.tradeyourplan.data.local.entity.AlarmEntity
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.data.local.entity.SettingEntity

@Database(
    entities = [QuoteEntity::class, AlarmEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TradeYourPlanDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun alarmDao(): AlarmDao
    abstract fun settingsDao(): SettingsDao
}
