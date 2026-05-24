package com.tradeyourplan.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.SettingsRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import com.tradeyourplan.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val updateQuoteUseCase: UpdateQuoteUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "QuoteViewModel"
    }

    private val _filterCategory = MutableStateFlow<Category?>(null)
    val filterCategory: StateFlow<Category?> = _filterCategory.asStateFlow()

    // 观察语录来源设置
    private val quoteSourceFilter: StateFlow<QuoteSource?> = settingsRepository.quoteSource
        .map { sourceStr ->
            Log.d(TAG, "quoteSource setting: $sourceStr")
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

    // 根据来源和分类过滤语录
    val quotes: StateFlow<List<Quote>> = combine(
        getQuotesUseCase(),
        quoteSourceFilter,
        _filterCategory
    ) { allQuotes, sourceFilter, categoryFilter ->
        var result = if (sourceFilter == null) {
            allQuotes // MIXED - 返回所有
        } else {
            allQuotes.filter { it.source == sourceFilter }
        }
        if (categoryFilter != null) {
            result = result.filter { it.category == categoryFilter }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val favoriteQuotes: StateFlow<List<Quote>> = getQuotesUseCase.getFavoriteQuotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    var showAddDialog by mutableStateOf(false)
        private set

    fun showAddDialog() {
        showAddDialog = true
    }

    fun hideAddDialog() {
        showAddDialog = false
    }

    fun setFilterCategory(category: Category?) {
        _filterCategory.value = category
    }

    fun addQuote(
        content: String,
        category: Category,
        marketType: MarketType
    ) {
        viewModelScope.launch {
            val quote = Quote(
                content = content,
                category = category,
                marketType = marketType,
                source = QuoteSource.USER
            )
            addQuoteUseCase(quote)
        }
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            updateQuoteUseCase(quote)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            deleteQuoteUseCase(quote)
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
        }
    }
}
