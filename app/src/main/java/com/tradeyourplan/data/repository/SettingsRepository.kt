// app/src/main/java/com/tradeyourplan/data/repository/SettingsRepository.kt
package com.tradeyourplan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
}
