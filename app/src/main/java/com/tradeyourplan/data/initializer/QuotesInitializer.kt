// app/src/main/java/com/tradeyourplan/data/initializer/QuotesInitializer.kt
package com.tradeyourplan.data.initializer

import android.content.Context
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

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val existingQuotes = quoteDao.getSystemQuotes()
        if (existingQuotes.isEmpty()) {
            loadQuotesFromAssets()
        }
    }

    private suspend fun loadQuotesFromAssets() {
        try {
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            val quoteType = object : TypeToken<List<QuoteDto>>() {}.type
            val quoteDtos: List<QuoteDto> = gson.fromJson(json, quoteType)

            val entities = quoteDtos.map { dto ->
                QuoteEntity(
                    content = dto.content,
                    category = dto.category,
                    marketType = dto.marketType,
                    source = dto.source
                )
            }
            quoteDao.insertQuotes(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class QuoteDto(
        val content: String,
        val category: String,
        val marketType: String,
        val source: String
    )
}
