package com.aicompanion.pro.ai

import android.content.Context
import android.content.Intent
import com.aicompanion.pro.data.AppDatabase
import com.aicompanion.pro.data.MemoryEntity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.jsonPrimitive

class ToolRegistry(
    private val ctx: Context,
    private val db: AppDatabase
) {
    fun declarations(): List<ToolDecl> = listOf(
        ToolDecl(
            listOf(
                FunctionDecl(
                    name = "take_note",
                    description = "احفظ ملاحظة سريعة للمستخدم",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("text") {
                                put("type", "string")
                                put("description", "نص الملاحظة")
                            }
                        }
                        putJsonArray("required") { add("text") }
                    }
                ),
                FunctionDecl(
                    name = "open_app",
                    description = "افتح تطبيق على الجهاز بواسطة اسم الحزمة",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("package_name") {
                                put("type", "string")
                            }
                        }
                        putJsonArray("required") { add("package_name") }
                    }
                ),
                FunctionDecl(
                    name = "remember_fact",
                    description = "احفظ معلومة دائمة عن المستخدم لتذكرها لاحقاً",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("fact") {
                                put("type", "string")
                            }
                            putJsonObject("category") {
                                put("type", "string")
                                put("description", "نوع: preference / personal / game / work")
                            }
                        }
                        putJsonArray("required") { add("fact") }
                    }
                ),
                FunctionDecl(
                    name = "recall_facts",
                    description = "استرجع معلومات محفوظة عن المستخدم بناءً على فئة معينة",
                    parameters = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("category") {
                                put("type", "string")
                            }
                        }
                    }
                )
            )
        )
    )

    fun invoke(name: String, args: JsonObject): JsonObject = when (name) {
        "take_note" -> {
            val t = args["text"]?.jsonPrimitive?.content.orEmpty()
            runBlocking { db.memory().insert(MemoryEntity(text = t, category = "note")) }
            buildJsonObject { put("ok", true); put("saved", t) }
        }
        "open_app" -> {
            val pkg = args["package_name"]?.jsonPrimitive?.content.orEmpty()
            val launch = ctx.packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(launch)
                buildJsonObject { put("ok", true) }
            } else {
                buildJsonObject {
                    put("ok", false)
                    put("error", "package_not_found")
                }
            }
        }
        "remember_fact" -> {
            val f = args["fact"]?.jsonPrimitive?.content.orEmpty()
            val cat = args["category"]?.jsonPrimitive?.content ?: "general"
            runBlocking { db.memory().insert(MemoryEntity(text = f, category = cat)) }
            buildJsonObject { put("ok", true) }
        }
        "recall_facts" -> {
            val cat = args["category"]?.jsonPrimitive?.content
            val list = runBlocking {
                if (cat != null) db.memory().byCategory(cat) else db.memory().all()
            }
            buildJsonObject {
                put("ok", true)
                putJsonArray("facts") {
                    list.take(20).forEach { add(it.text) }
                }
            }
        }
        else -> buildJsonObject {
            put("ok", false)
            put("error", "unknown_tool")
        }
    }
}
