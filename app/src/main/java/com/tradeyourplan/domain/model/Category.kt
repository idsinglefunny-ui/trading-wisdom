package com.tradeyourplan.domain.model

enum class Category(val displayName: String) {
    RISK_MGMT("风险管理"),
    MINDSET("交易心态"),
    DISCIPLINE("交易纪律"),
    TECHNICAL("技术分析");

    companion object {
        fun fromString(value: String): Category? {
            return values().find { it.name == value }
        }
    }
}
