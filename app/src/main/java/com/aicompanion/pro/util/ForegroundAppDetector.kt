package com.aicompanion.pro.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

object ForegroundAppDetector {
    fun detect(ctx: Context): String? = try {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 60_000
        val events = usm.queryEvents(begin, end)
        var last: String? = null
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                last = e.packageName
            }
        }
        last
    } catch (_: Exception) { null }
}
