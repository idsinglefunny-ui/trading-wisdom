// app/src/main/java/com/tradeyourplan/data/local/dao/SettingsDao.kt
package com.tradeyourplan.data.local.dao

import androidx.room.*
import com.tradeyourplan.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT value FROM settings WHERE key = :key")
    fun getSettingValueFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    @Query("INSERT OR REPLACE INTO settings (key, value) VALUES (:key, :value)")
    suspend fun setSettingValue(key: String, value: String)
}
