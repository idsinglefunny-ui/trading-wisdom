package com.tradeyourplan.data.sync

import com.tradeyourplan.data.api.ApiCategory
import com.tradeyourplan.data.api.ApiMarketType
import com.tradeyourplan.data.api.QuoteApiService
import com.tradeyourplan.data.api.QuoteResponse
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val quoteApiService: QuoteApiService,
    private val quoteDao: QuoteDao
) {
    /**
     * 同步系统语录
     * @return SyncResult 包含同步结果信息
     */
    suspend fun syncSystemQuotes(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val response = quoteApiService.getSystemQuotes()

            if (response.code != 0 || response.data == null) {
                return@withContext SyncResult(
                    success = false,
                    error = response.message
                )
            }

            val apiQuotes = response.data
            val existingQuotes = quoteDao.getSystemQuotes()
            val existingIds = existingQuotes.map { it.id }.toSet()

            // 分类需要新增/更新的语录
            val toInsert = mutableListOf<QuoteEntity>()
            val toUpdate = mutableListOf<QuoteEntity>()
            var unchanged = 0

            apiQuotes.forEach { apiQuote ->
                val entity = apiQuote.toEntity()
                if (entity.id in existingIds) {
                    // 检查是否需要更新
                    val existing = existingQuotes.find { it.id == entity.id }
                    if (existing != null && existing.content != entity.content) {
                        toUpdate.add(entity)
                    } else {
                        unchanged++
                    }
                } else {
                    toInsert.add(entity)
                }
            }

            // 执行数据库操作
            if (toInsert.isNotEmpty()) {
                quoteDao.insertQuotes(toInsert)
            }

            if (toUpdate.isNotEmpty()) {
                toUpdate.forEach { quoteDao.updateQuote(it) }
            }

            SyncResult(
                success = true,
                added = toInsert.size,
                updated = toUpdate.size,
                unchanged = unchanged,
                total = apiQuotes.size
            )

        } catch (e: Exception) {
            SyncResult(
                success = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * 获取随机语录（从服务器）
     * @param category 可选分类
     * @param marketType 可选市场类型
     * @return QuoteResponse 或 null
     */
    suspend fun fetchRandomQuote(
        category: Category? = null,
        marketType: MarketType? = null
    ): QuoteResponse? = withContext(Dispatchers.IO) {
        try {
            val response = quoteApiService.getRandomQuote(
                category = category?.name,
                marketType = marketType?.name
            )

            if (response.code == 0 && response.data != null) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 同步结果
 */
data class SyncResult(
    val success: Boolean,
    val added: Int = 0,
    val updated: Int = 0,
    val unchanged: Int = 0,
    val total: Int = 0,
    val error: String? = null
)

/**
 * 扩展函数：将 API QuoteResponse 转换为 QuoteEntity
 */
private fun QuoteResponse.toEntity() = QuoteEntity(
    id = id,
    content = content,
    category = category,
    marketType = marketType,
    source = source,
    isFavorite = isFavorite,
    createdAt = createdAt
)
