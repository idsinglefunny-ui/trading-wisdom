package com.tradeyourplan.domain.usecase

import android.util.Log
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import javax.inject.Inject

/**
 * 获取语录列表用例 - 在线优先模式
 * 用于一次性获取语录列表（非 Flow）
 */
class GetQuotesOnlineUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    companion object {
        private const val TAG = "GetQuotesOnlineUseCase"
    }

    /**
     * 获取语录列表 - 在线优先
     * @param category 可选分类筛选
     * @param marketType 可选市场类型筛选
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 语录列表
     */
    suspend operator fun invoke(
        category: Category? = null,
        marketType: MarketType? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Quote> {
        Log.d(TAG, "Fetching quotes list (online-first mode)")
        return repository.getQuotesOnline(category, marketType, limit, offset)
    }
}
