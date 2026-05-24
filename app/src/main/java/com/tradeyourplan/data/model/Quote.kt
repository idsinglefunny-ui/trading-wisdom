package com.tradeyourplan.data.model

import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource

data class Quote(
    val id: Long = 0,
    val content: String,
    val category: Category,
    val marketType: MarketType,
    val source: QuoteSource,
    val isFavorite: Boolean = false,
    val viewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
