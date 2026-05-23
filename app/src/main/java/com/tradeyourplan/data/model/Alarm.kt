package com.tradeyourplan.data.model

import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode

data class Alarm(
    val id: Long = 0,
    val type: AlarmType,
    val hour: Int? = null,
    val minute: Int? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val repeatMode: RepeatMode = RepeatMode.DAILY,
    val isEnabled: Boolean = true,
    val notificationLevel: NotificationLevel = NotificationLevel.NORMAL
) {
    val timeDisplay: String
        get() = when (type) {
            AlarmType.FIXED -> String.format("%02d:%02d", hour ?: 0, minute ?: 0)
            AlarmType.RANDOM -> String.format("%02d:%02d-%02d:%02d",
                startHour ?: 9, startMinute ?: 0,
                endHour ?: 15, endMinute ?: 0)
        }
}
