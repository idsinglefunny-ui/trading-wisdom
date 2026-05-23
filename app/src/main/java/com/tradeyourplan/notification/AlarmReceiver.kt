package com.tradeyourplan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val EXTRA_ALARM_ID = "alarm_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received: action=${intent.action}")

        val notificationHelper = NotificationHelper(context)

        // 默认语录，如果无法从数据库获取
        val defaultQuote = "计划你的交易，交易你的计划。"

        try {
            notificationHelper.showQuoteNotification(defaultQuote)
            Log.d(TAG, "Notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }
}
