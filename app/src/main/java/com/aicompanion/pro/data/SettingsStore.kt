package com.aicompanion.pro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("settings")

class SettingsStore(private val ctx: Context) {
    private object K {
        val VOICE = stringPreferencesKey("voice")
        val LANG = stringPreferencesKey("lang")
        val USER = stringPreferencesKey("user_name")
        val WAKE = booleanPreferencesKey("require_wake")
        val PROACTIVE = booleanPreferencesKey("proactive")
    }

    private val master = MasterKey.Builder(ctx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val secure = EncryptedSharedPreferences.create(
        ctx, "secure", master,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val apiKey: Flow<String> = ctx.ds.data.map {
        secure.getString("api_key", "").orEmpty()
    }

    fun saveApiKey(v: String) {
        secure.edit().putString("api_key", v).apply()
    }

    val voice: Flow<String> = ctx.ds.data.map { it[K.VOICE] ?: "Puck" }
    val language: Flow<String> = ctx.ds.data.map { it[K.LANG] ?: "ar-XA" }
    val userName: Flow<String?> = ctx.ds.data.map { it[K.USER] }
    val requireWakeWord: Flow<Boolean> = ctx.ds.data.map { it[K.WAKE] ?: false }
    val proactive: Flow<Boolean> = ctx.ds.data.map { it[K.PROACTIVE] ?: false }

    suspend fun setVoice(v: String) = ctx.ds.edit { it[K.VOICE] = v }
    suspend fun setLanguage(v: String) = ctx.ds.edit { it[K.LANG] = v }
    suspend fun setUserName(v: String) = ctx.ds.edit { it[K.USER] = v }
    suspend fun setRequireWake(v: Boolean) = ctx.ds.edit { it[K.WAKE] = v }
    suspend fun setProactive(v: Boolean) = ctx.ds.edit { it[K.PROACTIVE] = v }
}
