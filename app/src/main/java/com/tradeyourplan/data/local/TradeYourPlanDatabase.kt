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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite不支持DROP COLUMN（3.35.0之前），重建表
        db.execSQL("""
            CREATE TABLE alarms_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                hour INTEGER,
                minute INTEGER,
                startHour INTEGER,
                startMinute INTEGER,
                endHour INTEGER,
                endMinute INTEGER,
                repeatMode TEXT NOT NULL,
                isEnabled INTEGER NOT NULL,
                notificationLevel TEXT NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO alarms_new (id, type, hour, minute, startHour, startMinute, endHour, endMinute, repeatMode, isEnabled, notificationLevel)
            SELECT id, type, hour, minute, startHour, startMinute, endHour, endMinute, repeatMode, isEnabled, notificationLevel FROM alarms
        """)
        db.execSQL("DROP TABLE alarms")
        db.execSQL("ALTER TABLE alarms_new RENAME TO alarms")
    }
}

@Database(
    entities = [QuoteEntity::class, AlarmEntity::class, SettingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class TradeYourPlanDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun alarmDao(): AlarmDao
    abstract fun settingsDao(): SettingsDao
}
