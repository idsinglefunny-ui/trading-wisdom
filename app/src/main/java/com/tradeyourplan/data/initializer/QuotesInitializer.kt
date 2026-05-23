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
            val existingSystemQuotes = quoteDao.getSystemQuotes()
            Log.d(TAG, "Existing system quotes count: ${existingSystemQuotes.size}")

            if (existingSystemQuotes.size < 50) {
                Log.d(TAG, "System quotes count less than 50, loading from assets")
                loadQuotesFromAssets(existingSystemQuotes)
            } else {
                Log.d(TAG, "System quotes exist (${existingSystemQuotes.size}), skipping load")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing quotes", e)
            loadQuotesFromAssets(emptyList())
        }
    }

    private suspend fun loadQuotesFromAssets(existingQuotes: List<QuoteEntity>) {
        try {
            Log.d(TAG, "Loading quotes from assets")
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "JSON content length: ${json.length}")

            val quoteType = object : TypeToken<List<QuoteDto>>() {}.type
            val quoteDtos: List<QuoteDto> = gson.fromJson(json, quoteType)
            Log.d(TAG, "Parsed ${quoteDtos.size} quotes from JSON")

            // 直接删除，避免实体类型转换问题
            if (existingQuotes.isNotEmpty()) {
                try {
                    existingQuotes.forEach {
                        quoteDao.deleteQuoteById(it.id)
                    }
                    Log.d(TAG, "Deleted ${existingQuotes.size} existing system quotes")
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting existing quotes", e)
                }
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
