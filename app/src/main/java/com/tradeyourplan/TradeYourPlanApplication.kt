// app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
package com.tradeyourplan

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TradeYourPlanApplication : Application() {

    @Inject
    lateinit var quotesInitializer: com.tradeyourplan.data.initializer.QuotesInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("TradeYourPlanApp", "Application onCreate started")

        // Delete old notification channels to ensure fresh creation
        deleteOldNotificationChannels()

        applicationScope.launch {
            try {
                Log.d("TradeYourPlanApp", "About to initialize quotes")
                quotesInitializer.initializeIfNeeded()
                Log.d("TradeYourPlanApp", "Quotes initialization completed")
            } catch (e: Exception) {
                Log.e("TradeYourPlanApp", "Failed to initialize quotes", e)
            }
        }
    }

    private fun deleteOldNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = getSystemService(NotificationManager::class.java)
                // Delete ALL old notification channels
                listOf("trade_quotes_silent", "trade_quotes_normal", "trade_quotes_fullscreen",
                       "trade_quotes_silent_v2", "trade_quotes_normal_v2", "trade_quotes_fullscreen_v2").forEach { channelId ->
                    manager.deleteNotificationChannel(channelId)
                    Log.d("TradeYourPlanApp", "Deleted notification channel: $channelId")
                }
            } catch (e: Exception) {
                Log.e("TradeYourPlanApp", "Failed to delete notification channels", e)
            }
        }
    }
}
