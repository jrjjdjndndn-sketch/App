package com.aicompanion.pro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aicompanion.pro.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { SettingsStore(ctx) }
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var voice by remember { mutableStateOf("Puck") }
    var lang by remember { mutableStateOf("ar-XA") }
    var user by remember { mutableStateOf("") }
    var requireWake by remember { mutableStateOf(false) }
    var proactive by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }
    var langMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apiKey = store.apiKey.first()
        voice = store.voice.first()
        lang = store.language.first()
        user = store.userName.first().orEmpty()
        requireWake = store.requireWakeWord.first()
        proactive = store.proactive.first()
    }

    val voices = listOf("Puck", "Charon", "Kore", "Fenrir", "Aoede")
    val langs = listOf(
        "ar-XA" to "العربية",
        "en-US" to "English",
        "fr-FR" to "Français",
        "es-ES" to "Español",
        "de-DE" to "Deutsch",
        "tr-TR" to "Türkçe",
        "hi-IN" to "हिन्दी"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        Column(
            Modifier.padding(p).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("مفتاح Gemini API", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "احصل عليه مجاناً من aistudio.google.com",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("الصوت واللغة", style = MaterialTheme.typography.titleMedium)
                    ExposedDropdownMenuBox(
                        expanded = voiceMenu,
                        onExpandedChange = { voiceMenu = it }
                    ) {
                        OutlinedTextField(
                            value = voice,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("صوت الرفيق") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(voiceMenu)
                            }
                        )
                        androidx.compose.material3.ExposedDropdownMenu(
                            expanded = voiceMenu,
                            onDismissRequest = { voiceMenu = false }
                        ) {
                            voices.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = { voice = it; voiceMenu = false }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = langMenu,
                        onExpandedChange = { langMenu = it }
                    ) {
                        OutlinedTextField(
                            value = langs.firstOrNull { it.first == lang }?.second ?: lang,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اللغة") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(langMenu)
                            }
                        )
                        androidx.compose.material3.ExposedDropdownMenu(
                            expanded = langMenu,
                            onDismissRequest = { langMenu = false }
                        ) {
                            langs.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { lang = code; langMenu = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("اسمك (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "كلمة الاستيقاظ",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "لن يرد إلا بعد \"يا رفيق\" أو الضغط المطوّل",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = requireWake,
                            onCheckedChange = { requireWake = it }
                        )
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "الوضع الاستباقي",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "يعلّق تلقائياً على أحداث استثنائية فقط",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = proactive,
                            onCheckedChange = { proactive = it }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        store.saveApiKey(apiKey.trim())
                        store.setVoice(voice)
                        store.setLanguage(lang.trim())
                        store.setUserName(user.trim())
                        store.setRequireWake(requireWake)
                        store.setProactive(proactive)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("حفظ") }
        }
    }
}
