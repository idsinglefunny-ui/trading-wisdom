package com.tradeyourplan.data.model

import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode

data class Alarm(
    val id: Long = 0,
    val type: AlarmType,
    val hour: Int? = null,           // FIXED 类型用
    val minute: Int? = null,         // FIXED 类型用
    val startHour: Int? = null,      // RANDOM 类型用
    val endHour: Int? = null,        // RANDOM 类型用
    val targetPackage: String? = null, // EVENT_TRIGGERED 类型用
    val delaySeconds: Int? = null,   // EVENT_TRIGGERED 类型用
    val repeatMode: RepeatMode = RepeatMode.DAILY,
    val isEnabled: Boolean = true,
    val notificationLevel: NotificationLevel = NotificationLevel.NORMAL
) {
    val timeDisplay: String
        get() = when (type) {
            AlarmType.FIXED -> String.format("%02d:%02d", hour ?: 0, minute ?: 0)
            AlarmType.RANDOM -> String.format("%02d:00-%02d:00", startHour ?: 9, endHour ?: 15)
            AlarmType.EVENT_TRIGGERED -> "事件触发"
        }
}
