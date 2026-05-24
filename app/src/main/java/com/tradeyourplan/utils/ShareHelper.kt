package com.tradeyourplan.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.tradeyourplan.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShareHelper {

    /**
     * 分享语录为图片（带二维码）
     */
    suspend fun shareQuoteAsImage(
        context: Context,
        quoteText: String,
        themeMode: ThemeMode
    ): Intent = withContext(Dispatchers.Default) {
        val imageUri = QuoteShareImageGenerator.generateShareImage(
            context.applicationContext,
            quoteText,
            themeMode
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * 创建分享选择器 Intent
     */
    fun createShareChooser(shareIntent: Intent, title: String = "分享语录"): Intent {
        return Intent.createChooser(shareIntent, title)
    }

    /**
     * 检查是否可以安全启动分享（需要先解锁锁屏）
     */
    fun canStartShareOnLockScreen(): Boolean {
        // Android 8.1+ 可以在锁屏上启动其他 activity
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }
}
