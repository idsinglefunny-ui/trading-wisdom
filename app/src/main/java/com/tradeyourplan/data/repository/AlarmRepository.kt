// app/src/main/java/com/tradeyourplan/data/repository/AlarmRepository.kt
package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.AlarmDao
import com.tradeyourplan.data.local.entity.AlarmEntity
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomainModel()
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomainModel() }
    }

    suspend fun addAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarmById(id: Long) {
        alarmDao.deleteAlarmById(id)
    }

    suspend fun setAlarmEnabled(id: Long, enabled: Boolean) {
        alarmDao.setAlarmEnabled(id, enabled)
    }

    private fun AlarmEntity.toDomainModel() = Alarm(
        id = id,
        type = AlarmType.fromString(type) ?: AlarmType.FIXED,
        hour = hour,
        minute = minute,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        targetPackage = targetPackage,
        delaySeconds = delaySeconds,
        repeatMode = RepeatMode.fromString(repeatMode) ?: RepeatMode.DAILY,
        isEnabled = isEnabled,
        notificationLevel = NotificationLevel.fromString(notificationLevel) ?: NotificationLevel.NORMAL
    )

    private fun Alarm.toEntity() = AlarmEntity(
        id = id,
        type = type.name,
        hour = hour,
        minute = minute,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        targetPackage = targetPackage,
        delaySeconds = delaySeconds,
        repeatMode = repeatMode.name,
        isEnabled = isEnabled,
        notificationLevel = notificationLevel.name
    )
}
