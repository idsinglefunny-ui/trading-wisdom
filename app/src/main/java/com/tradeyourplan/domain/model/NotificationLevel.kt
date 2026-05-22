package com.tradeyourplan.domain.model

enum class NotificationLevel(val displayName: String) {
    SILENT("静默"),
    NORMAL("标准"),
    FULL_SCREEN("强提醒");

    companion object {
        fun fromString(value: String): NotificationLevel? {
            return values().find { it.name == value }
        }
    }
}
