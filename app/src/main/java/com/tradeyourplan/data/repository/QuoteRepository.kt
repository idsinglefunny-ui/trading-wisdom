// app/src/main/java/com/tradeyourplan/data/repository/QuoteRepository.kt
package com.tradeyourplan.data.repository

import android.util.Log
import com.tradeyourplan.data.api.QuoteApiService
import com.tradeyourplan.data.api.QuoteResponse
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
    private val quoteDao: QuoteDao,
    private val quoteApiService: QuoteApiService
) {
    companion object {
        private const val TAG = "QuoteRepository"
    }

    // ========== 本地数据方法 ==========

    fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getQuoteById(id: Long): Quote? {
        return quoteDao.getQuoteById(id)?.toDomainModel()
    }

    suspend fun getQuoteByContent(content: String): Quote? {
        return quoteDao.getQuoteByContent(content)?.toDomainModel()
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

    // ========== 在线优先方法 (API 优先，失败时降级到本地) ==========

    /**
     * 获取随机语录 - 在线优先
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     * @return Quote 或 null (在线和本地都没有时)
     */
    suspend fun getRandomQuoteOnline(
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        // 先尝试从 API 获取
        return try {
            Log.d(TAG, "Fetching random quote from API...")
            val response = quoteApiService.getRandomQuote(
                category = category?.name,
                marketType = marketType?.name
            )

            if (response.code == 0 && response.data != null) {
                Log.d(TAG, "Successfully fetched quote from API")
                response.data.toDomainModel()
            } else {
                Log.w(TAG, "API returned non-success code: ${response.code}, message: ${response.message}")
                // 降级到本地
                getRandomQuote(null, category, marketType)
            }
        } catch (e: Exception) {
            Log.w(TAG, "API request failed, falling back to local: ${e.message}")
            // API 失败，降级到本地
            getRandomQuote(null, category, marketType)
        }
    }

    /**
     * 获取语录列表 - 在线优先
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 语录列表
     */
    suspend fun getQuotesOnline(
        category: Category? = null,
        marketType: MarketType? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Quote> {
        return try {
            Log.d(TAG, "Fetching quotes from API...")
            val response = quoteApiService.getQuotes(
                category = category?.name,
                marketType = marketType?.name,
                limit = limit,
                offset = offset
            )

            if (response.code == 0 && response.data != null) {
                Log.d(TAG, "Successfully fetched ${response.data.quotes.size} quotes from API")
                response.data.quotes.map { it.toDomainModel() }
            } else {
                Log.w(TAG, "API returned non-success code: ${response.code}")
                // 降级到本地 (本地不支持分页，返回所有)
                getAllQuotesLocal(category)
            }
        } catch (e: Exception) {
            Log.w(TAG, "API request failed, falling back to local: ${e.message}")
            // 降级到本地
            getAllQuotesLocal(category)
        }
    }

    /**
     * 获取所有语录 - 在线优先
     * @param category 可选分类筛选
     * @return 语录列表
     */
    suspend fun getAllQuotesOnline(category: Category? = null): List<Quote> {
        return getQuotesOnline(category, limit = 100) // 获取更多
    }

    // ========== 辅助方法 ==========

    private suspend fun getAllQuotesLocal(category: Category?): List<Quote> {
        val allQuotes = quoteDao.getAllQuotesLocal()
        return if (category == null) {
            allQuotes.map { it.toDomainModel() }
        } else {
            allQuotes.filter { it.category == category.name }
                .map { it.toDomainModel() }
        }
    }

    private fun QuoteEntity.toDomainModel() = Quote(
        id = id,
        content = content,
        category = Category.fromString(category) ?: Category.DISCIPLINE,
        marketType = MarketType.fromString(marketType) ?: MarketType.GENERAL,
        source = QuoteSource.fromString(source) ?: QuoteSource.SYSTEM,
        isFavorite = isFavorite,
        viewCount = viewCount,
        createdAt = createdAt
    )

    private fun QuoteResponse.toDomainModel() = Quote(
        id = id,
        content = content,
        category = Category.fromString(category) ?: Category.DISCIPLINE,
        marketType = MarketType.fromString(marketType) ?: MarketType.GENERAL,
        source = QuoteSource.fromString(source) ?: QuoteSource.SYSTEM,
        isFavorite = isFavorite,
        viewCount = viewCount,
        createdAt = createdAt
    )

    private fun Quote.toEntity() = QuoteEntity(
        id = id,
        content = content,
        category = category.name,
        marketType = marketType.name,
        source = source.name,
        isFavorite = isFavorite,
        viewCount = viewCount,
        createdAt = createdAt
    )
}
