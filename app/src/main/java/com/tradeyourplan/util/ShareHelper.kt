package com.tradeyourplan.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.tradeyourplan.data.model.Quote

class ShareHelper(private val context: Context) {

    fun shareQuote(quote: Quote) {
        val shareText = formatShareText(quote)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "分享语录").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(shareIntent)
    }

    private fun formatShareText(quote: Quote): String {
        return """
            ${quote.content}

            — 交易你的计划
            #${quote.category.name} #交易智慧
        """.trimIndent()
    }
}