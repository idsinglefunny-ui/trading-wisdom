// app/src/main/java/com/tradeyourplan/data/local/TradeYourPlanDatabase.kt
package com.tradeyourplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tradeyourplan.data.local.dao.AlarmDao
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.dao.SettingsDao
import com.tradeyourplan.data.local.entity.AlarmEntity
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.data.local.entity.SettingEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE alarms ADD COLUMN startMinute INTEGER DEFAULT 0")
        db.execSQL("ALTER TABLE alarms ADD COLUMN endMinute INTEGER DEFAULT 0")
    }
}

@Database(
    entities = [QuoteEntity::class, AlarmEntity::class, SettingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TradeYourPlanDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun alarmDao(): AlarmDao
    abstract fun settingsDao(): SettingsDao
}
