// app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
package com.tradeyourplan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TradeYourPlanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化预置数据将在 Task 6 中实现
    }
}
