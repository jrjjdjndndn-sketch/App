package com.aicompanion.pro.util

import com.aicompanion.pro.data.MessageEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Exporter {
    fun toJson(messages: List<MessageEntity>): String {
        val json = Json { prettyPrint = true; encodeDefaults = true }
        return json.encodeToString(messages)
    }

    fun toMarkdown(messages: List<MessageEntity>): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return buildString {
            append("# AI Companion Conversation\n\n")
            messages.forEach { m ->
                val who = if (m.role == "user") "**أنت**" else "**الرفيق**"
                append("$who · ${df.format(Date(m.createdAt))}\n\n")
                append(m.text).append("\n\n---\n\n")
            }
        }
    }
}
