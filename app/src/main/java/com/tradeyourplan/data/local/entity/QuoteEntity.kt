// app/src/main/java/com/tradeyourplan/data/local/entity/QuoteEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: String,        // Category.name
    val marketType: String,      // MarketType.name
    val source: String,          // QuoteSource.name
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
