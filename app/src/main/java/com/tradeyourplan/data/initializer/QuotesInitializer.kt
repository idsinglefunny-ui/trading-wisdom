// app/src/main/java/com/tradeyourplan/data/initializer/QuotesInitializer.kt
package com.tradeyourplan.data.initializer

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语录初始化器 - 在线优先模式
 * 仅在本地无数据时加载内置语录作为离线降级方案
 */
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

            // 只有本地完全没数据时，才加载内置语录
            if (existingSystemQuotes.isEmpty()) {
                Log.d(TAG, "No local quotes found, loading built-in quotes as fallback")
                loadQuotesFromAssets()
            } else {
                Log.d(TAG, "Local quotes exist (${existingSystemQuotes.size}), app will use online API")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing quotes", e)
        }
    }

    private suspend fun loadQuotesFromAssets() {
        try {
            Log.d(TAG, "Loading built-in quotes from assets")
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "JSON content length: ${json.length}")

            val quoteType = object : TypeToken<List<QuoteDto>>() {}.type
            val quoteDtos: List<QuoteDto> = gson.fromJson(json, quoteType)
            Log.d(TAG, "Parsed ${quoteDtos.size} quotes from JSON")

            // 插入内置语录作为离线降级方案
            val entities = quoteDtos.map { dto ->
                QuoteEntity(
                    content = dto.content,
                    category = dto.category,
                    marketType = dto.marketType,
                    source = dto.source
                )
            }

            quoteDao.insertQuotes(entities)
            Log.d(TAG, "Inserted ${entities.size} built-in quotes to database")

            // 验证插入
            val verifyCount = quoteDao.getSystemQuotes().size
            Log.d(TAG, "Verification: system quotes count after insert: $verifyCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading built-in quotes from assets", e)
        }
    }

    private data class QuoteDto(
        val content: String,
        val category: String,
        val marketType: String,
        val source: String
    )
}
