// app/src/main/java/com/tradeyourplan/ui/main/MainViewModel.kt
package com.tradeyourplan.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.usecase.GetRandomQuoteUseCase
import com.tradeyourplan.domain.usecase.ToggleFavoriteUseCase
import com.tradeyourplan.ui.theme.ThemeMode
import com.tradeyourplan.data.repository.SettingsRepository
import com.tradeyourplan.domain.model.QuoteSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .map { modeStr ->
            when (modeStr) {
                "WARM_ENCOURAGING" -> ThemeMode.WARM_ENCOURAGING
                "MINIMAL_LIGHT" -> ThemeMode.MINIMAL_LIGHT
                else -> ThemeMode.PROFESSIONAL_DARK
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.PROFESSIONAL_DARK
        )

    private val quoteSource: StateFlow<QuoteSource?> = settingsRepository.quoteSource
        .map { sourceStr ->
            when (sourceStr) {
                "SYSTEM" -> QuoteSource.SYSTEM
                "USER" -> QuoteSource.USER
                else -> null // MIXED
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        // 监听quoteSource变化，自动重新加载
        viewModelScope.launch {
            quoteSource.collect { loadRandomQuote() }
        }
    }

    fun loadRandomQuote() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            val sourceFilter = quoteSource.value
            val quote = getRandomQuoteUseCase(sourceFilter)
            _uiState.value = if (quote != null) {
                MainUiState.Success(quote)
            } else {
                MainUiState.Empty("暂无语录，请添加语录或检查应用设置")
            }
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
            // 重新加载以更新状态
            loadRandomQuote()
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val quote: Quote) : MainUiState()
    data class Empty(val message: String = "暂无语录") : MainUiState()
}
