package com.tradeyourplan.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix

object QRCodeGenerator {

    private const val GITHUB_REPO_URL = "https://trading.tangping.me/"

    /**
     * Generate QR code bitmap for GitHub repository
     * @param size Size of the QR code (width = height)
     * @return QR code bitmap
     */
    fun generateRepoQRCode(size: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
        )

        val bitMatrix: BitMatrix = writer.encode(GITHUB_REPO_URL, BarcodeFormat.QR_CODE, size, size, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}
