package com.tradeyourplan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import com.tradeyourplan.domain.usecase.GetRandomQuoteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getRandomQuoteUseCase: GetRandomQuoteUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            val quote = getRandomQuoteUseCase()
            quote?.let {
                notificationHelper.showQuoteNotification(it)
            }
        }
    }
}
