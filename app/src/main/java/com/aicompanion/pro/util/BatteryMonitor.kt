package com.aicompanion.pro.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BatteryMonitor(private val ctx: Context) {
    private val _level = MutableStateFlow(100)
    val level: StateFlow<Int> = _level

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val l = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) ?: 100
            val s = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            _level.value = (l * 100) / s.coerceAtLeast(1)
        }
    }

    fun start() {
        val f = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        runCatching { ctx.registerReceiver(receiver, f) }
    }

    fun stop() {
        runCatching { ctx.unregisterReceiver(receiver) }
    }

    fun isLowBattery(): Boolean = _level.value <= 20
}
