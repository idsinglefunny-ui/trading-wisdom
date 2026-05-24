package com.tradeyourplan.utils

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.core.content.FileProvider
import com.tradeyourplan.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object QuoteShareImageGenerator {

    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1920

    suspend fun generateShareImage(
        context: Context,
        quoteText: String,
        themeMode: ThemeMode
    ): android.net.Uri = withContext(Dispatchers.Default) {
        val qrBitmap = QRCodeGenerator.generateRepoQRCode(256)
        val bitmap = createShareBitmap(
            context = context,
            quoteText = quoteText,
            qrBitmap = qrBitmap,
            themeMode = themeMode
        )
        saveBitmapToFile(context, bitmap)
    }

    private fun createShareBitmap(
        context: Context,
        quoteText: String,
        qrBitmap: Bitmap,
        themeMode: ThemeMode
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = getThemeColors(context, themeMode)
        canvas.drawColor(colors.backgroundColor)
        drawShareContent(canvas, quoteText, qrBitmap, colors)

        return bitmap
    }

    private fun getThemeColors(context: Context, themeMode: ThemeMode): ThemeColors {
        return when (themeMode) {
            ThemeMode.WARM_ENCOURAGING -> ThemeColors(
                backgroundColor = 0xFF1A1A1A.toInt(),
                primaryColor = 0xFFFFB74D.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
                secondaryColor = 0xFF9E9E9E.toInt(),
                cardColor = 0xFF2D2D2D.toInt(),
                borderColor = 0xFF404040.toInt()
            )
            ThemeMode.MINIMAL_LIGHT -> ThemeColors(
                backgroundColor = 0xFFFFFFFF.toInt(),
                primaryColor = 0xFF1976D2.toInt(),
                textColor = 0xFF1A1A1A.toInt(),
                secondaryColor = 0xFF757575.toInt(),
                cardColor = 0xFFF5F5F5.toInt(),
                borderColor = 0xFFE0E0E0.toInt()
            )
            ThemeMode.PROFESSIONAL_DARK -> ThemeColors(
                backgroundColor = 0xFF0D1117.toInt(),
                primaryColor = 0xFF58A6FF.toInt(),
                textColor = 0xFFE6EDF3.toInt(),
                secondaryColor = 0xFF8B949E.toInt(),
                cardColor = 0xFF161B22.toInt(),
                borderColor = 0xFF30363D.toInt()
            )
        }
    }

    private fun drawShareContent(canvas: Canvas, quoteText: String, qrBitmap: Bitmap, colors: ThemeColors) {
        val width = IMAGE_WIDTH.toFloat()
        val height = IMAGE_HEIGHT.toFloat()

        val spacingXS = 24f
        val spacingS = 36f
        val spacingL = 72f
        val spacingXXL = 144f

        // Brand
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primaryColor
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("交易智慧", spacingXXL, spacingXXL, brandPaint)

        // Brand line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primaryColor
            strokeWidth = 4f
            alpha = 80
        }
        val brandWidth = brandPaint.measureText("交易智慧")
        canvas.drawLine(spacingXXL, spacingXXL + spacingXS, spacingXXL + brandWidth, spacingXXL + spacingXS, linePaint)

        // Calculate text layout
        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textColor
            textSize = 96f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.02f
            }
        }

        val maxTextWidth = width - (spacingXXL * 2.5f) // Leave room for margins
        val lines = breakTextIntoLines(quoteText, quotePaint, maxTextWidth)

        // Determine layout mode
        val layoutMode = when (lines.size) {
            1 -> LayoutMode.CENTER
            2 -> LayoutMode.LEFT_RIGHT
            else -> LayoutMode.ALL_CENTER
        }

        // Draw quote
        val lineHeight = 140f
        val totalQuoteHeight = lines.size * lineHeight
        val availableTop = spacingXXL * 3
        val availableBottom = height - spacingXXL * 4
        val availableHeight = availableBottom - availableTop
        val quoteStartY = availableTop + (availableHeight - totalQuoteHeight) / 2

        val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primaryColor
            textSize = 96f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.02f
            }
        }

        val bracketOpen = "「"
        val bracketClose = "」"

        when (layoutMode) {
            LayoutMode.CENTER -> {
                // Single line, centered with brackets
                val fullText = "$bracketOpen${lines[0]}$bracketClose"
                quotePaint.textAlign = Paint.Align.CENTER
                canvas.drawText(fullText, width / 2, quoteStartY + lineHeight, quotePaint)
            }
            LayoutMode.LEFT_RIGHT -> {
                // Two lines: first left, second right
                quotePaint.textAlign = Paint.Align.LEFT

                // First line with open bracket
                val firstText = "$bracketOpen${lines[0]}"
                val x1 = spacingXXL * 1.2f
                canvas.drawText(firstText, x1, quoteStartY + lineHeight, quotePaint)

                // Second line with close bracket, right aligned
                quotePaint.textAlign = Paint.Align.RIGHT
                val secondText = "${lines[1]}$bracketClose"
                val x2 = width - spacingXXL * 1.2f
                canvas.drawText(secondText, x2, quoteStartY + lineHeight * 2, quotePaint)
            }
            LayoutMode.ALL_CENTER -> {
                // Three or more lines, all centered
                quotePaint.textAlign = Paint.Align.CENTER

                lines.forEachIndexed { index, line ->
                    val displayLine = when {
                        index == 0 && lines.size > 1 -> "$bracketOpen$line"
                        index == lines.size - 1 -> "$line$bracketClose"
                        else -> line
                    }
                    canvas.drawText(displayLine, width / 2, quoteStartY + (index + 1) * lineHeight, quotePaint)
                }
            }
        }

        // Attribution
        val attrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.secondaryColor
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.05f
            }
        }
        canvas.drawText("—— 交易你的计划", spacingXXL, height - spacingXXL, attrPaint)

        // QR Code
        val qrSize = 120f
        val qrMargin = spacingXXL
        val qrLeft = width - qrSize - qrMargin
        val qrTop = height - qrSize - spacingS - spacingXXL

        // QR shadow
        val shadowPaint = Paint().apply {
            color = 0x40000000.toInt()
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(qrLeft + 8f, qrTop + 8f, qrLeft + qrSize + 8f, qrTop + qrSize + 8f),
            16f, 16f, shadowPaint
        )

        // QR background
        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.cardColor
        }
        canvas.drawRoundRect(
            RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize),
            16f, 16f, qrBgPaint
        )

        // QR border
        val qrBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.borderColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize),
            16f, 16f, qrBorderPaint
        )

        // Draw QR
        val qrPadding = 8f
        canvas.drawBitmap(
            qrBitmap,
            null,
            RectF(qrLeft + qrPadding, qrTop + qrPadding, qrLeft + qrSize - qrPadding, qrTop + qrSize - qrPadding),
            null
        )

        // Scan text
        val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.secondaryColor
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("扫码下载", qrLeft + qrSize, qrTop - spacingXS, scanPaint)
    }

    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        // First, calculate how many characters fit in one line
        val maxCharsPerLine = estimateMaxChars(text, paint, maxWidth)

        // Calculate rough line count
        val totalChars = text.length
        val estimatedLines = (totalChars + maxCharsPerLine - 1) / maxCharsPerLine

        return when {
            totalChars <= maxCharsPerLine -> {
                // Fits in one line
                listOf(text)
            }
            estimatedLines == 2 || totalChars <= maxCharsPerLine * 2 -> {
                // Will be 2 lines - split evenly
                splitIntoTwoBalancedLines(text, paint, maxWidth)
            }
            else -> {
                // 3 or more lines - use standard breaking
                breakTextStandard(text, paint, maxWidth)
            }
        }
    }

    private fun estimateMaxChars(text: String, paint: Paint, maxWidth: Float): Int {
        // Sample a few characters to estimate average width
        val sample = text.take(min(20, text.length))
        val totalWidth = paint.measureText(sample)
        val avgCharWidth = totalWidth / sample.length
        return (maxWidth / avgCharWidth).toInt()
    }

    private fun splitIntoTwoBalancedLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val totalLength = text.length
        val midPoint = totalLength / 2

        // Find best split point near middle, preferring punctuation
        val searchRange = min(20, totalLength / 4) // Search within 20 chars of middle

        // First try to find punctuation after mid point
        val punctuations = listOf('，', '。', '；', '！', '？', '、')
        for (offset in 0..searchRange) {
            for (punct in punctuations) {
                if (midPoint + offset < totalLength && text[midPoint + offset] == punct) {
                    val firstLine = text.substring(0, midPoint + offset + 1)
                    val secondLine = text.substring(midPoint + offset + 1)
                    return listOf(firstLine, secondLine)
                }
                if (midPoint - offset > 0 && text[midPoint - offset] == punct) {
                    val firstLine = text.substring(0, midPoint - offset + 1)
                    val secondLine = text.substring(midPoint - offset + 1)
                    return listOf(firstLine, secondLine)
                }
            }
        }

        // No punctuation found, split at middle
        val firstLine = text.substring(0, midPoint)
        val secondLine = text.substring(midPoint)

        // Verify both lines fit within width, adjust if needed
        val firstLineWidth = paint.measureText(firstLine)
        val secondLineWidth = paint.measureText(secondLine)

        return when {
            firstLineWidth > maxWidth -> {
                // First line too long, find actual break point
                val breakPoint = findBreakPoint(text, paint, maxWidth)
                listOf(text.substring(0, breakPoint), text.substring(breakPoint))
            }
            else -> listOf(firstLine, secondLine)
        }
    }

    private fun findBreakPoint(text: String, paint: Paint, maxWidth: Float): Int {
        val count = paint.breakText(text, 0, text.length, true, maxWidth, null)
        return count.toInt()
    }

    private fun breakTextStandard(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val textLength = text.length
        var start = 0

        while (start < textLength) {
            val count = paint.breakText(text, start, textLength, true, maxWidth, null)

            if (start + count >= textLength) {
                lines.add(text.substring(start))
                break
            }

            // Find good break point
            var breakPos = start + count
            val substring = text.substring(start, breakPos)

            // Look for punctuation
            val punctuations = listOf('，', '。', '；', '！', '？', '、', ' ', '\n')
            for (punct in punctuations) {
                val lastPunctPos = substring.lastIndexOf(punct)
                if (lastPunctPos > 0) {
                    breakPos = start + lastPunctPos + 1
                    break
                }
            }

            lines.add(text.substring(start, breakPos))
            start = breakPos
        }

        return lines
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): android.net.Uri {
        val cacheDir = File(context.cacheDir, "share_images")
        cacheDir.mkdirs()

        val file = File(cacheDir, "trading_wisdom_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private data class ThemeColors(
        val backgroundColor: Int,
        val primaryColor: Int,
        val textColor: Int,
        val secondaryColor: Int,
        val cardColor: Int,
        val borderColor: Int
    )

    private enum class LayoutMode {
        CENTER,      // 1 line
        LEFT_RIGHT,  // 2 lines
        ALL_CENTER   // 3+ lines
    }
}
