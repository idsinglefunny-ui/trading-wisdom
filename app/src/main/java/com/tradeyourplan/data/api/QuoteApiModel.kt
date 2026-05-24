package com.tradeyourplan.data.api

import com.google.gson.annotations.SerializedName

// API Response wrapper
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?
)

// Quote model from API
data class QuoteResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("content")
    val content: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("categoryDisplay")
    val categoryDisplay: String,
    @SerializedName("marketType")
    val marketType: String,
    @SerializedName("marketTypeDisplay")
    val marketTypeDisplay: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("isFavorite")
    val isFavorite: Boolean,
    @SerializedName("viewCount")
    val viewCount: Int,
    @SerializedName("createdAt")
    val createdAt: Long
)

// System quotes list response
data class SystemQuotesResponse(
    @SerializedName("quotes")
    val quotes: List<QuoteResponse>
)

// Category enum
enum class ApiCategory(val value: String, val displayName: String) {
    @SerializedName("RISK_MGMT")
    RISK_MGMT("RISK_MGMT", "风险管理"),
    @SerializedName("MINDSET")
    MINDSET("MINDSET", "交易心态"),
    @SerializedName("DISCIPLINE")
    DISCIPLINE("DISCIPLINE", "交易纪律"),
    @SerializedName("TECHNICAL")
    TECHNICAL("TECHNICAL", "技术分析");

    companion object {
        fun fromValue(value: String): ApiCategory? {
            return values().find { it.value == value }
        }
    }
}

// Market type enum
enum class ApiMarketType(val value: String, val displayName: String) {
    @SerializedName("STOCK")
    STOCK("STOCK", "股票"),
    @SerializedName("FUTURES")
    FUTURES("FUTURES", "期货"),
    @SerializedName("GENERAL")
    GENERAL("GENERAL", "通用");

    companion object {
        fun fromValue(value: String): ApiMarketType? {
            return values().find { it.value == value }
        }
    }
}
