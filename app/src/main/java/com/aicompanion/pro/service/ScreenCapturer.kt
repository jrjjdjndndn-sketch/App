package com.aicompanion.pro.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager

class ScreenCapturer(private val ctx: Context) {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var vd: VirtualDisplay? = null
    private var w = 0
    private var h = 0

    fun start(code: Int, data: Intent) {
        val mgr = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        projection = mgr.getMediaProjection(code, data)
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val m = DisplayMetrics().also {
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(it)
        }
        val scale = 0.5f
        w = (m.widthPixels * scale).toInt()
        h = (m.heightPixels * scale).toInt()
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        vd = projection?.createVirtualDisplay(
            "cap", w, h, m.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface, null, Handler(Looper.getMainLooper())
        )
    }

    fun captureLatest(): Bitmap? {
        val img = reader?.acquireLatestImage() ?: return null
        return try {
            val p = img.planes[0]
            val pad = p.rowStride - p.pixelStride * w
            val bmp = Bitmap.createBitmap(
                w + pad / p.pixelStride, h, Bitmap.Config.ARGB_8888
            )
            bmp.copyPixelsFromBuffer(p.buffer)
            Bitmap.createBitmap(bmp, 0, 0, w, h)
        } finally {
            img.close()
        }
    }

    fun stop() {
        vd?.release()
        reader?.close()
        projection?.stop()
        projection = null
    }
}
