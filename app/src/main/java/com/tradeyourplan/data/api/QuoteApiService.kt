package com.tradeyourplan.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteApiService {

    /**
     * 获取系统预设语录（用于App初始化）
     */
    @GET("system/quotes")
    suspend fun getSystemQuotes(): ApiResponse<List<QuoteResponse>>

    /**
     * 获取随机语录
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     */
    @GET("quotes/random")
    suspend fun getRandomQuote(
        @Query("category") category: String? = null,
        @Query("marketType") marketType: String? = null
    ): ApiResponse<QuoteResponse>

    /**
     * 获取语录列表
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     * @param limit 每页数量，默认20，最大100
     * @param offset 偏移量，默认0
     */
    @GET("quotes")
    suspend fun getQuotes(
        @Query("category") category: String? = null,
        @Query("marketType") marketType: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): ApiResponse<QuoteListResponse>

    /**
     * 获取分类列表
     */
    @GET("quotes/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryLabel>>

    /**
     * 获取市场类型列表
     */
    @GET("quotes/market-types")
    suspend fun getMarketTypes(): ApiResponse<List<MarketTypeLabel>>
}

// Supporting data classes
data class QuoteListResponse(
    val quotes: List<QuoteResponse>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class CategoryLabel(
    val value: String,
    val label: String
)

data class MarketTypeLabel(
    val value: String,
    val label: String
)
