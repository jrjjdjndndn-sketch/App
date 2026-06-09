package com.aicompanion.pro.audio

class WakeWordGate(private val requireWake: Boolean) {
    @Volatile private var armedUntil = 0L

    fun isArmed(): Boolean {
        if (!requireWake) return true
        return System.currentTimeMillis() < armedUntil
    }

    fun arm(durationMs: Long) {
        armedUntil = System.currentTimeMillis() + durationMs
    }

    fun disarm() { armedUntil = 0L }
}
