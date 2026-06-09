package com.aicompanion.pro.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ImageEncoder {
    fun toWebp(src: Bitmap, quality: Int = 65, maxWidth: Int = 1024): ByteArray {
        val scaled = if (src.width > maxWidth) {
            val ratio = maxWidth.toFloat() / src.width
            Bitmap.createScaledBitmap(
                src, maxWidth, (src.height * ratio).toInt(), true
            )
        } else src
        val out = ByteArrayOutputStream()
        @Suppress("DEPRECATION")
        scaled.compress(Bitmap.CompressFormat.WEBP, quality, out)
        return out.toByteArray()
    }
}
