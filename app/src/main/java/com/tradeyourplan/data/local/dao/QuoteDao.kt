// app/src/main/java/com/tradeyourplan/data/local/dao/QuoteDao.kt
package com.tradeyourplan.data.local.dao

import androidx.room.*
import com.tradeyourplan.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE source = 'SYSTEM'")
    suspend fun getSystemQuotes(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE source = 'USER'")
    suspend fun getUserQuotes(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category = :category")
    fun getQuotesByCategory(category: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE marketType = :marketType")
    fun getQuotesByMarketType(marketType: String): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Long)

    @Query("UPDATE quotes SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
