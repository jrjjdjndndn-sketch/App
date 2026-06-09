package com.aicompanion.pro.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.aicompanion.pro.R
import com.aicompanion.pro.ai.CompanionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OverlayManager(
    private val ctx: Context,
    private val scope: CoroutineScope,
    private val state: StateFlow<CompanionState>,
    private val muted: StateFlow<Boolean>,
    private val onTap: () -> Unit,
    private val onLongPressStart: () -> Unit,
    private val onLongPressEnd: () -> Unit
) {
    private var view: View? = null
    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun show() {
        if (view != null || !canOverlay()) return

        val v = LayoutInflater.from(ctx).inflate(R.layout.overlay_bubble, null)
        val icon = v.findViewById<ImageView>(R.id.icon)
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 240
        }

        var dx = 0f
        var dy = 0f
        var px = 0
        var py = 0
        var moved = false
        var longTriggered = false
        val longPressMs = 500L

        v.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = e.rawX
                    dy = e.rawY
                    px = lp.x
                    py = lp.y
                    moved = false
                    longTriggered = false
                    v.postDelayed({
                        if (!moved && view != null) {
                            longTriggered = true
                            onLongPressStart()
                        }
                    }, longPressMs)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val nx = px - (e.rawX - dx).toInt()
                    val ny = py + (e.rawY - dy).toInt()
                    val dist = kotlin.math.hypot(
                        (e.rawX - dx).toDouble(),
                        (e.rawY - dy).toDouble()
                    )
                    if (dist > 14) {
                        moved = true
                        lp.x = nx
                        lp.y = ny
                        wm.updateViewLayout(v, lp)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (longTriggered) onLongPressEnd()
                    else if (!moved) onTap()
                    true
                }
                else -> false
            }
        }

        wm.addView(v, lp)
        view = v

        scope.launch {
            combine(state, muted) { s, m -> s to m }.collect { (s, m) ->
                icon.setImageResource(
                    when {
                        m -> R.drawable.ic_bubble_muted
                        s == CompanionState.LISTENING -> R.drawable.ic_bubble_listening
                        s == CompanionState.THINKING -> R.drawable.ic_bubble_thinking
                        s == CompanionState.SPEAKING -> R.drawable.ic_bubble_speaking
                        s == CompanionState.RECONNECTING -> R.drawable.ic_bubble_thinking
                        else -> R.drawable.ic_bubble_idle
                    }
                )
            }
        }
    }

    fun hide() {
        view?.let { runCatching { wm.removeView(it) }; view = null }
    }

    private fun canOverlay(): Boolean =
        Build.VERSION.SDK_INT < 23 || android.provider.Settings.canDrawOverlays(ctx)
}
