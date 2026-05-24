package com.tradeyourplan.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.room.Room
import com.tradeyourplan.data.local.MIGRATION_1_2
import com.tradeyourplan.data.local.MIGRATION_2_3
import com.tradeyourplan.data.local.TradeYourPlanDatabase
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received: action=${intent.action}")
        android.util.Log.d("AlarmReceiver", "=== ALARM TRIGGERED ===")

        val quoteText = getRandomQuote(context)
        Log.d(TAG, "Quote: $quoteText")
        android.util.Log.d("AlarmReceiver", "Quote: $quoteText")

        // Try to wake up the screen
        wakeUpScreen(context)

        // Post notification with fullScreenIntent
        val notificationHelper = NotificationHelper(context)
        try {
            notificationHelper.showQuoteNotification(quoteText)
            Log.d(TAG, "Notification posted successfully")
            android.util.Log.d("AlarmReceiver", "Notification posted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification", e)
            android.util.Log.e("AlarmReceiver", "Error posting notification", e)
        }

        // Direct activity launch
        try {
            val overlayIntent = Intent(context, QuoteReminderActivity::class.java)
            overlayIntent.putExtra(QuoteReminderActivity.EXTRA_QUOTE_TEXT, quoteText)
            overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(overlayIntent)
            Log.d(TAG, "Direct activity launch attempted")
            android.util.Log.d("AlarmReceiver", "Direct activity launch attempted")
        } catch (e: Exception) {
            Log.e(TAG, "Direct activity launch failed", e)
            android.util.Log.e("AlarmReceiver", "Direct activity launch failed", e)
        }

        // 4. Vibrate
        vibrate(context)
        android.util.Log.d("AlarmReceiver", "=== ALARM PROCESSING COMPLETE ===")
    }

    private fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            // Use FULL_WAKE_LOCK to wake up screen and keep it on
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "TradeYourPlan:AlarmWakeLock"
            )
            wakeLock.acquire(10000) // Hold for 10 seconds to ensure notification shows
            Log.d(TAG, "Screen woken up with FULL_WAKE_LOCK for 10 seconds")
        } catch (e: Exception) {
            Log.e(TAG, "Error waking screen", e)
        }
    }

    private fun getRandomQuote(context: Context): String {
        return try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                TradeYourPlanDatabase::class.java,
                "tradeyourplan.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()

            val quote = runBlocking {
                val allQuotes = db.quoteDao().getSystemQuotes() + db.quoteDao().getUserQuotes()
                if (allQuotes.isNotEmpty()) allQuotes.random() else null
            }
            db.close()
            quote?.content ?: "计划你的交易，交易你的计划。"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching quote", e)
            "计划你的交易，交易你的计划。"
        }
    }

    private fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }
}
