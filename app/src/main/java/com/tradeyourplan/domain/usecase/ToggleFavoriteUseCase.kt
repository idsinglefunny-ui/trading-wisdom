package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.toggleFavorite(id)
    }
}
