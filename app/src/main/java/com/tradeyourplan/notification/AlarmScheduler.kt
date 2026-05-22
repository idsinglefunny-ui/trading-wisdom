package com.tradeyourplan.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val ALARM_REQUEST_CODE_BASE = 1000
        private const val ACTION_ALARM_TRIGGERED = "com.tradeyourplan.ALARM_TRIGGERED"
        private const val EXTRA_ALARM_ID = "alarm_id"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        when (alarm.type) {
            AlarmType.FIXED -> scheduleFixedAlarm(alarm)
            AlarmType.RANDOM -> scheduleRandomAlarm(alarm)
            AlarmType.EVENT_TRIGGERED -> scheduleEventAlarm(alarm)
        }
    }

    private fun scheduleFixedAlarm(alarm: Alarm) {
        if (alarm.hour == null || alarm.minute == null) return

        val intent = createAlarmIntent(alarm.id)
        val pendingIntent = createPendingIntent(alarm.id, intent)

        val triggerTime = getTriggerTime(alarm.hour, alarm.minute)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun scheduleRandomAlarm(alarm: Alarm) {
        if (alarm.startHour == null || alarm.endHour == null) return

        val intent = createAlarmIntent(alarm.id)
        val pendingIntent = createPendingIntent(alarm.id, intent)

        val startTime = getTriggerTime(alarm.startHour, 0)
        val endTime = getTriggerTime(alarm.endHour, 0)
        val windowLengthMillis = endTime - startTime

        try {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                startTime,
                windowLengthMillis.coerceAtLeast(60000), // At least 1 minute window
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun scheduleEventAlarm(alarm: Alarm) {
        // Placeholder for event-based triggers
        // This will be implemented when event detection is added
        // For now, we can log or store the alarm configuration
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = createAlarmIntent(alarmId)
        val pendingIntent = createPendingIntent(alarmId, intent)
        alarmManager.cancel(pendingIntent)
    }

    private fun createAlarmIntent(alarmId: Long): Intent {
        return Intent(ACTION_ALARM_TRIGGERED).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            setPackage(context.packageName)
        }
    }

    private fun createPendingIntent(alarmId: Long, intent: Intent): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            (ALARM_REQUEST_CODE_BASE + alarmId).toInt(),
            intent,
            flags
        )
    }

    private fun getTriggerTime(hour: Int, minute: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        val now = System.currentTimeMillis()
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return calendar.timeInMillis
    }
}
