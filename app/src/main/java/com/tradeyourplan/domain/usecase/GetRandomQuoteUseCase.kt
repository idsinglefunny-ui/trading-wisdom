package com.tradeyourplan.domain.usecase

import android.util.Log
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import javax.inject.Inject

class GetRandomQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    companion object {
        private const val TAG = "GetRandomQuoteUseCase"
    }

    /**
     * 获取随机语录 - 在线优先模式
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     * @param useOnline 是否使用在线优先（默认 true）
     * @return Quote 或 null
     */
    suspend operator fun invoke(
        category: Category? = null,
        marketType: MarketType? = null,
        useOnline: Boolean = true
    ): Quote? {
        return if (useOnline) {
            // 在线优先：先尝试 API，失败降级到本地
            Log.d(TAG, "Fetching random quote (online-first mode)")
            repository.getRandomQuoteOnline(category, marketType)
        } else {
            // 纯本地模式
            Log.d(TAG, "Fetching random quote (local-only mode)")
            repository.getRandomQuote(null, category, marketType)
        }
    }

    /**
     * @deprecated 使用 invoke(useOnline = false) 代替
     * 保留以兼容旧代码
     */
    suspend fun getLocalRandomQuote(
        sourceFilter: QuoteSource? = null,
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        return repository.getRandomQuote(sourceFilter, category, marketType)
    }
}
