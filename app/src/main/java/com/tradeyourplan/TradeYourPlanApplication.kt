// app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
package com.tradeyourplan

import android.app.Application
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
        applicationScope.launch {
            quotesInitializer.initializeIfNeeded()
        }
    }
}
