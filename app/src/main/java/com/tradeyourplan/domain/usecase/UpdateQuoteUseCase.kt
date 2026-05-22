package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class UpdateQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: Quote) {
        repository.updateQuote(quote)
    }
}
