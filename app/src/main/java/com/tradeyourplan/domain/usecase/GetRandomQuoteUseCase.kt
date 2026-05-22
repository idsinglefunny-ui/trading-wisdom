package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import javax.inject.Inject

class GetRandomQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(
        sourceFilter: QuoteSource? = null,
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        return repository.getRandomQuote(sourceFilter, category, marketType)
    }
}
