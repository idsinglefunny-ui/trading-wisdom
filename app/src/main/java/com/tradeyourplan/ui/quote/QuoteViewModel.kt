package com.tradeyourplan.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Quote
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val quotes: StateFlow<List<Quote>> = getQuotesUseCase()
        .stateIn(
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

    private val _filterCategory = MutableStateFlow<Category?>(null)
    val filterCategory: StateFlow<Category?> = _filterCategory.asStateFlow()

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
