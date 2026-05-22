package com.tradeyourplan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.repository.SettingsRepository
import com.tradeyourplan.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.PROFESSIONAL_DARK
        )

    val quoteSource: StateFlow<String> = settingsRepository.quoteSource
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "MIXED"
        )

    val notificationLevel: StateFlow<String> = settingsRepository.notificationLevel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "NORMAL"
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode.name)
        }
    }

    fun setQuoteSource(source: String) {
        viewModelScope.launch {
            settingsRepository.setQuoteSource(source)
        }
    }

    fun setNotificationLevel(level: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationLevel(level)
        }
    }
}
