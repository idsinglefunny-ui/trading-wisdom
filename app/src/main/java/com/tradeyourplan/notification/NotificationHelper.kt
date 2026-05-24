package com.tradeyourplan.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tradeyourplan.R
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.ui.main.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_SILENT = "trade_quotes_silent_v2"
        const val CHANNEL_NORMAL = "trade_quotes_normal_v2"
        const val CHANNEL_FULLSCREEN = "trade_quotes_fullscreen_v2"
        const val CHANNEL_FULLSCREEN_V2 = "trade_quotes_fullscreen_v2"
        private const val NOTIFICATION_ID = 1001
        private const val PREVIEW_NOTIFICATION_ID = 2001
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete ALL old channels to ensure fresh creation with correct settings
            listOf("trade_quotes_silent", "trade_quotes_normal", "trade_quotes_fullscreen",
                   CHANNEL_SILENT, CHANNEL_NORMAL, CHANNEL_FULLSCREEN,
                   "trade_quotes_silent_v2", "trade_quotes_normal_v2", "trade_quotes_fullscreen_v2").forEach {
                try {
                    manager.deleteNotificationChannel(it)
                    Log.d(TAG, "Deleted notification channel: $it")
                } catch (e: Exception) {
                    // Channel doesn't exist, ignore
                }
            }

            // Silent channel
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_SILENT, "静默提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "无声音无震动"
                    enableVibration(false)
                    setSound(null, null)
                    setShowBadge(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    enableLights(true)
                }
            )

            // Normal channel - HIGH importance required for fullScreenIntent
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_NORMAL, "标准提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "声音 + 震动 + 弹窗"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 100, 300)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    enableLights(true)
                    lightColor = android.graphics.Color.BLUE
                }
            )

            // Full screen channel
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_FULLSCREEN, "强提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "大声 + 长震动 + 弹窗"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                }
            )

            Log.d(TAG, "Notification channels created with new IDs")
        }
    }

    fun showQuoteNotification(quoteText: String, level: NotificationLevel = NotificationLevel.NORMAL) {
        showNotification(quoteText, level, NOTIFICATION_ID)
    }

    fun showQuoteNotification(quote: Quote, level: NotificationLevel = NotificationLevel.NORMAL) {
        showNotification(quote.content, level, NOTIFICATION_ID)
    }

    fun preview(level: NotificationLevel) {
        val previewText = when (level) {
            NotificationLevel.SILENT -> "这是一条静默提醒预览（无声音、无震动）"
            NotificationLevel.NORMAL -> "这是一条标准提醒预览（有声音、有震动）"
            NotificationLevel.FULL_SCREEN -> "这是一条强提醒预览（大声、长震动）"
        }
        showNotification(previewText, level, PREVIEW_NOTIFICATION_ID)
        vibrate(level)
    }

    private fun showNotification(text: String, level: NotificationLevel, notificationId: Int) {
        val channelId = when (level) {
            NotificationLevel.SILENT -> CHANNEL_SILENT
            NotificationLevel.NORMAL -> CHANNEL_NORMAL
            NotificationLevel.FULL_SCREEN -> CHANNEL_FULLSCREEN
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Click notification → open MainActivity
        val contentIntent = Intent(context, MainActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, pendingFlags)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("交易智慧")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        if (level == NotificationLevel.FULL_SCREEN) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        // For alarm notifications (NOT preview), attach fullScreenIntent to launch overlay
        if (notificationId == NOTIFICATION_ID) {
            val overlayIntent = Intent(context, QuoteReminderActivity::class.java)
            overlayIntent.putExtra(QuoteReminderActivity.EXTRA_QUOTE_TEXT, text)
            // Enhanced flags for better lock screen behavior on Huawei
            overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context, 1, overlayIntent, pendingFlags or PendingIntent.FLAG_CANCEL_CURRENT
            )

            // Use BigTextStyle with full content for better lock screen display
            builder.setStyle(NotificationCompat.BigTextStyle()
                .bigText(text)
                .setBigContentTitle("交易智慧")
                .setSummaryText(""))

            // Set fullScreenIntent with high priority - this should expand on lock screen
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            builder.setOngoing(true)
            builder.setAutoCancel(false)
            builder.setOnlyAlertOnce(false)

            // Add sound and vibration for alarm
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)

            Log.d(TAG, "fullScreenIntent set for notification with MAX priority and ALARM category")
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d(TAG, "Notification posted to channel: $channelId, id: $notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException posting notification", e)
        }
    }

    private fun vibrate(level: NotificationLevel) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        when (level) {
            NotificationLevel.SILENT -> { /* no vibration */ }
            NotificationLevel.NORMAL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, 128))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
            NotificationLevel.FULL_SCREEN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                }
            }
        }
    }

    fun cancelPreview() {
        NotificationManagerCompat.from(context).cancel(PREVIEW_NOTIFICATION_ID)
    }
}
