package com.tradeyourplan.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType
import java.text.SimpleDateFormat
import java.util.*

class AlarmScheduler(private val context: Context) {

    companion object {
        private const val ALARM_REQUEST_CODE_BASE = 1000
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val TAG = "AlarmScheduler"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        Log.d(TAG, "scheduleAlarm called: id=${alarm.id}, type=${alarm.type}, enabled=${alarm.isEnabled}, hour=${alarm.hour}, minute=${alarm.minute}")
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
        val currentTime = System.currentTimeMillis()
        Log.d(TAG, "Scheduling fixed alarm for ${String.format("%02d:%02d", alarm.hour, alarm.minute)}")
        Log.d(TAG, "Current time: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.CHINA).format(Date(currentTime))}")
        Log.d(TAG, "Trigger time: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.CHINA).format(Date(triggerTime))}")
        Log.d(TAG, "Time from now: ${(triggerTime - currentTime) / 1000}s")

        try {
            // Use setAlarmClock() for guaranteed delivery on Huawei devices
            // setAlarmClock has special system privileges and cannot be delayed by app standby
            val showPendingIntent = createShowPendingIntent(alarm)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
                pendingIntent
            )
            Log.d(TAG, "Successfully scheduled with setAlarmClock (guaranteed delivery)")
        } catch (e: Exception) {
            Log.e(TAG, "Exception with setAlarmClock, falling back to setAndAllowWhileIdle", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Fallback to setAndAllowWhileIdle succeeded")
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback also failed", e2)
            }
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

    private fun createShowPendingIntent(alarm: Alarm): PendingIntent? {
        // Create a PendingIntent for showing the alarm icon in status bar
        // This is used by setAlarmClock to indicate an active alarm
        val showIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.tradeyourplan.ALARM_SHOW"
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
            context,
            (ALARM_REQUEST_CODE_BASE + alarm.id + 50000).toInt(),
            showIntent,
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
