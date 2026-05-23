// app/src/main/java/com/tradeyourplan/data/initializer/QuotesInitializer.kt
package com.tradeyourplan.data.initializer

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotesInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quoteDao: QuoteDao
) {
    private val gson = Gson()
    private val TAG = "QuotesInitializer"

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // 检查系统语录数量，如果少于50条（我们知道有100条），就重新加载
            val existingSystemQuotes = quoteDao.getSystemQuotes()
            Log.d(TAG, "Existing system quotes count: ${existingSystemQuotes.size}")

            // 总是尝试加载，以防数据丢失
            // 如果系统语录少于预期数量，重新加载
            if (existingSystemQuotes.size < 50) {
                Log.d(TAG, "System quotes count less than 50, loading from assets")
                loadQuotesFromAssets()
            } else {
                Log.d(TAG, "System quotes exist (${existingSystemQuotes.size}), skipping load")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing quotes", e)
            // 出错时也尝试加载
            loadQuotesFromAssets()
        }
    }

    private suspend fun loadQuotesFromAssets() {
        try {
            Log.d(TAG, "Loading quotes from assets")
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "JSON content length: ${json.length}")

            val quoteType = object : TypeToken<List<QuoteDto>>() {}.type
            val quoteDtos: List<QuoteDto> = gson.fromJson(json, quoteType)
            Log.d(TAG, "Parsed ${quoteDtos.size} quotes from JSON")

            // 先删除所有现有的系统语录
            val existing = quoteDao.getSystemQuotes()
            if (existing.isNotEmpty()) {
                Log.d(TAG, "Deleting ${existing.size} existing system quotes")
                existing.forEach { quoteDao.deleteQuote(it) }
            }

            // 插入新的系统语录
            val entities = quoteDtos.map { dto ->
                QuoteEntity(
                    content = dto.content,
                    category = dto.category,
                    marketType = dto.marketType,
                    source = dto.source
                )
            }

            quoteDao.insertQuotes(entities)
            Log.d(TAG, "Inserted ${entities.size} quotes to database")

            // 验证插入
            val verifyCount = quoteDao.getSystemQuotes().size
            Log.d(TAG, "Verification: system quotes count after insert: $verifyCount")

            if (verifyCount < quoteDtos.size) {
                Log.w(TAG, "Warning: Inserted ${quoteDtos.size} but only $verifyCount verified")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quotes from assets", e)
        }
    }

    private data class QuoteDto(
        val content: String,
        val category: String,
        val marketType: String,
        val source: String
    )
}
