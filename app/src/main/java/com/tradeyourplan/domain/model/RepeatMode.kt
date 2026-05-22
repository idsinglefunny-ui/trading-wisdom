package com.tradeyourplan.domain.model

enum class RepeatMode(val displayName: String) {
    ONCE("一次"),
    DAILY("每天"),
    WEEKDAYS("工作日"),
    CUSTOM("自定义");

    companion object {
        fun fromString(value: String): RepeatMode? {
            return values().find { it.name == value }
        }
    }
}
