package com.aicompanion.pro.util

import android.graphics.Bitmap

object PerceptualHash {
    fun dhash(bmp: Bitmap): Long {
        val small = Bitmap.createScaledBitmap(bmp, 9, 8, true)
        var hash = 0L
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val l = luma(small.getPixel(x, y))
                val r = luma(small.getPixel(x + 1, y))
                if (l < r) hash = hash or (1L shl (y * 8 + x))
            }
        }
        return hash
    }

    private fun luma(p: Int): Int {
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    fun hamming(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)
}
