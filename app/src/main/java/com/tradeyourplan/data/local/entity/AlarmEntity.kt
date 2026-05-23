// app/src/main/java/com/tradeyourplan/data/local/entity/AlarmEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val hour: Int? = null,
    val minute: Int? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val repeatMode: String,
    val isEnabled: Boolean = true,
    val notificationLevel: String
)
