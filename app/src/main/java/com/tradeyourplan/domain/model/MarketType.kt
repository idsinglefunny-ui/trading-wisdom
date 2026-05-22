package com.tradeyourplan.domain.model

enum class MarketType(val displayName: String) {
    STOCK("股票"),
    FUTURES("期货"),
    GENERAL("通用");

    companion object {
        fun fromString(value: String): MarketType? {
            return values().find { it.name == value }
        }
    }
}
