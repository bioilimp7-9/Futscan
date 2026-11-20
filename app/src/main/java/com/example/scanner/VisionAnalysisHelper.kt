package com.example.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PixelInspectionSummary(
    val avgRed: Int,
    val avgGreen: Int,
    val avgBlue: Int,
    val centerRed: Int,
    val centerGreen: Int,
    val centerBlue: Int,
    val averageLuminance: Int,
    val darkPixelPercent: Int,
    val descriptiveToneKz: String,
    val centerToneKz: String
)

object VisionAnalysisHelper {

    fun nowFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun inspectImageUri(context: Context, uri: Uri): PixelInspectionSummary? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = BitmapFactory.decodeStream(stream, null, options)
            stream.close()
            if (bitmap == null) return null
            val result = inspectBitmap(bitmap)
            bitmap.recycle()
            result
        } catch (_: Exception) {
            null
        }
    }

    fun inspectBitmap(bitmap: Bitmap): PixelInspectionSummary {
        val scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var darkCount = 0
        val total = 120 * 120

        var cRSum = 0L
        var cGSum = 0L
        var cBSum = 0L
        var cCount = 0

        for (y in 0 until 120) {
            for (x in 0 until 120) {
                val px = scaled.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF
                rSum += r
                gSum += g
                bSum += b
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                if (lum < 65) darkCount++

                if (x in 48..72 && y in 48..72) {
                    cRSum += r
                    cGSum += g
                    cBSum += b
                    cCount++
                }
            }
        }
        if (scaled != bitmap) scaled.recycle()

        val avgR = (rSum / total).toInt()
        val avgG = (gSum / total).toInt()
        val avgB = (bSum / total).toInt()
        val cR = (cRSum / cCount.coerceAtLeast(1)).toInt()
        val cG = (cGSum / cCount.coerceAtLeast(1)).toInt()
        val cB = (cBSum / cCount.coerceAtLeast(1)).toInt()
        val lum = (0.299 * avgR + 0.587 * avgG + 0.114 * avgB).toInt()
        val darkPct = ((darkCount.toFloat() / total) * 100f).toInt()

        return PixelInspectionSummary(
            avgRed = avgR,
            avgGreen = avgG,
            avgBlue = avgB,
            centerRed = cR,
            centerGreen = cG,
            centerBlue = cB,
            averageLuminance = lum,
            darkPixelPercent = darkPct,
            descriptiveToneKz = describeRgbKz(avgR, avgG, avgB),
            centerToneKz = describeRgbKz(cR, cG, cB)
        )
    }

    private fun describeRgbKz(r: Int, g: Int, b: Int): String {
        val hex = String.format(Locale.US, "#%02X%02X%02X", r, g, b)
        val tone = when {
            r > 180 && g < 110 && b < 110 -> "қызыл / алқызыл"
            r > 190 && g > 130 && b < 90 -> "қызғылт сары"
            r > 185 && g > 180 && b < 110 -> "сары"
            g > r + 20 && g > b + 15 -> "жасыл"
            b > r + 20 && b > g + 15 -> "көк / көгілдір"
            r > 120 && b > 120 && g < 110 -> "күлгін"
            r > 200 && g > 200 && b > 200 -> "ақшыл / ашық"
            r < 75 && g < 75 && b < 75 -> "қою қара"
            else -> "аралас табиғи реңк"
        }
        return "$tone (RGB: $r, $g, $b • $hex)"
    }
}
