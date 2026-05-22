// app/src/main/java/com/tradeyourplan/data/local/entity/AlarmEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,            // AlarmType.name
    val hour: Int? = null,
    val minute: Int? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val targetPackage: String? = null,
    val delaySeconds: Int? = null,
    val repeatMode: String,      // RepeatMode.name
    val isEnabled: Boolean = true,
    val notificationLevel: String // NotificationLevel.name
)
