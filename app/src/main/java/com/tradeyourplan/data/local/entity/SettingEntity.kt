// app/src/main/java/com/tradeyourplan/data/local/entity/SettingEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
