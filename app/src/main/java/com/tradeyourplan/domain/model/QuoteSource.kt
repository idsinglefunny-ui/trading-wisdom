package com.tradeyourplan.domain.model

enum class QuoteSource(val displayName: String) {
    SYSTEM("系统"),
    USER("用户");

    companion object {
        fun fromString(value: String): QuoteSource? {
            return values().find { it.name == value }
        }
    }
}
