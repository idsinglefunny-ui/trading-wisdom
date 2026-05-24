// app/src/main/java/com/tradeyourplan/data/repository/SettingsRepository.kt
package com.tradeyourplan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tradeyourplan.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val QUOTE_SOURCE = stringPreferencesKey("quote_source")
        private val NOTIFICATION_LEVEL = stringPreferencesKey("notification_level")

        // Sync related keys
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val SYNC_STATUS = stringPreferencesKey("sync_status")
        private val AUTO_SYNC_ENABLED = stringPreferencesKey("auto_sync_enabled")
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "PROFESSIONAL_DARK"
    }

    val quoteSource: Flow<String> = dataStore.data.map { preferences ->
        preferences[QUOTE_SOURCE] ?: "MIXED"
    }

    val notificationLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LEVEL] ?: "NORMAL"
    }

    val lastSyncTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: 0L
    }

    val syncStatus: Flow<String> = dataStore.data.map { preferences ->
        preferences[SYNC_STATUS] ?: "NEVER"
    }

    val autoSyncEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_SYNC_ENABLED] != "false"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setQuoteSource(source: String) {
        dataStore.edit { preferences ->
            preferences[QUOTE_SOURCE] = source
        }
    }

    suspend fun setNotificationLevel(level: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_LEVEL] = level
        }
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun setSyncStatus(status: SyncStatus) {
        dataStore.edit { preferences ->
            preferences[SYNC_STATUS] = status.name
        }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SYNC_ENABLED] = if (enabled) "true" else "false"
        }
    }
}

