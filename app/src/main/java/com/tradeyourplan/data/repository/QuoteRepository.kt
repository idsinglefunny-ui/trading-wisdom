// app/src/main/java/com/tradeyourplan/data/repository/QuoteRepository.kt
package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val quoteDao: QuoteDao
) {
    fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getQuoteById(id: Long): Quote? {
        return quoteDao.getQuoteById(id)?.toDomainModel()
    }

    fun getFavoriteQuotes(): Flow<List<Quote>> {
        return quoteDao.getFavoriteQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getRandomQuote(
        sourceFilter: QuoteSource? = null,
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        val quotes = when (sourceFilter) {
            QuoteSource.SYSTEM -> quoteDao.getSystemQuotes()
            QuoteSource.USER -> quoteDao.getUserQuotes()
            null -> {
                // 混合模式，获取所有
                buildList {
                    addAll(quoteDao.getSystemQuotes())
                    addAll(quoteDao.getUserQuotes())
                }
            }
        }

        val filtered = quotes.filter { entity ->
            val categoryMatch = category == null || entity.category == category.name
            val marketTypeMatch = marketType == null || entity.marketType == marketType.name
            categoryMatch && marketTypeMatch
        }

        return if (filtered.isNotEmpty()) {
            filtered.random().toDomainModel()
        } else null
    }

    fun getQuotesByCategory(category: Category): Flow<List<Quote>> {
        return quoteDao.getQuotesByCategory(category.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun addQuote(quote: Quote): Long {
        return quoteDao.insertQuote(quote.toEntity())
    }

    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote.toEntity())
    }

    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote.toEntity())
    }

    suspend fun deleteQuoteById(id: Long) {
        quoteDao.deleteQuoteById(id)
    }

    suspend fun toggleFavorite(id: Long) {
        quoteDao.toggleFavorite(id)
    }

    suspend fun insertInitialQuotes(quotes: List<Quote>) {
        quoteDao.insertQuotes(quotes.map { it.toEntity() })
    }

    private fun QuoteEntity.toDomainModel() = Quote(
        id = id,
        content = content,
        category = Category.fromString(category) ?: Category.DISCIPLINE,
        marketType = MarketType.fromString(marketType) ?: MarketType.GENERAL,
        source = QuoteSource.fromString(source) ?: QuoteSource.SYSTEM,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    private fun Quote.toEntity() = QuoteEntity(
        id = id,
        content = content,
        category = category.name,
        marketType = marketType.name,
        source = source.name,
        isFavorite = isFavorite,
        createdAt = createdAt
    )
}
