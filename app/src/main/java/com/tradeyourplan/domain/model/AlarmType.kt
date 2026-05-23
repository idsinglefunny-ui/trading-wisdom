package com.tradeyourplan.domain.model

enum class AlarmType(val displayName: String) {
    FIXED("固定时间"),
    RANDOM("随机时间");

    companion object {
        fun fromString(value: String): AlarmType? {
            return values().find { it.name == value }
        }
    }
}
