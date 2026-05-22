package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    operator fun invoke(category: Category? = null): Flow<List<Quote>> {
        return if (category == null) {
            repository.getAllQuotes()
        } else {
            repository.getQuotesByCategory(category)
        }
    }

    fun getFavoriteQuotes(): Flow<List<Quote>> {
        return repository.getFavoriteQuotes()
    }
}
