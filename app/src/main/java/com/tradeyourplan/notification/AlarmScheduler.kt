package com.tradeyourplan.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val ALARM_REQUEST_CODE_BASE = 1000
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val TAG = "AlarmScheduler"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        Log.d(TAG, "Scheduling alarm: id=${alarm.id}, type=${alarm.type}, enabled=${alarm.isEnabled}")
        when (alarm.type) {
            AlarmType.FIXED -> scheduleFixedAlarm(alarm)
            AlarmType.RANDOM -> scheduleRandomAlarm(alarm)
        }
    }

    private fun scheduleFixedAlarm(alarm: Alarm) {
        if (alarm.hour == null || alarm.minute == null) {
            Log.w(TAG, "Cannot schedule fixed alarm: hour or minute is null")
            return
        }

        val intent = createAlarmIntent(alarm.id)
        val pendingIntent = createPendingIntent(alarm.id, intent)

        val triggerTime = getTriggerTime(alarm.hour, alarm.minute)
        Log.d(TAG, "Scheduling fixed alarm for ${String.format("%02d:%02d", alarm.hour, alarm.minute)} at $triggerTime")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // 检查权限
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled with setExactAndAllowWhileIdle")
                } else {
                    Log.w(TAG, "Cannot schedule exact alarms - permission denied")
                    // 降级到不精确的闹钟
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled with setExact")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when scheduling alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception when scheduling alarm", e)
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
                windowLengthMillis.coerceAtLeast(60000),
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = createAlarmIntent(alarmId)
        val pendingIntent = createPendingIntent(alarmId, intent)
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm: id=$alarmId")
    }

    private fun createAlarmIntent(alarmId: Long): Intent {
        // 使用显式Intent
        return Intent(context, AlarmReceiver::class.java).apply {
            action = "com.tradeyourplan.ALARM_TRIGGERED"
            putExtra(EXTRA_ALARM_ID, alarmId)
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
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return calendar.timeInMillis
    }
}
