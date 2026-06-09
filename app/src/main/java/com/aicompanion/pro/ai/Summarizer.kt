package com.aicompanion.pro.ai

import com.aicompanion.pro.data.MessageEntity

object Summarizer {
    fun summarize(messages: List<MessageEntity>, maxChars: Int = 600): String {
        if (messages.isEmpty()) return ""
        val joined = messages.takeLast(40).joinToString("\n") {
            val role = if (it.role == "user") "U" else "A"
            "$role: ${it.text.take(120)}"
        }
        return joined.take(maxChars)
    }
}
