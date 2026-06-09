package com.aicompanion.pro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
    onStats: () -> Unit,
    onMemory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Companion Pro") },
                actions = {
                    IconButton(onClick = onMemory) { Icon(Icons.Default.Memory, null) }
                    IconButton(onClick = onStats) { Icon(Icons.Default.Analytics, null) }
                    IconButton(onClick = onHistory) { Icon(Icons.Default.History, null) }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null) }
                }
            )
        }
    ) { p ->
        Column(
            Modifier.padding(p).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("رفيقك الصامت", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "يرى شاشتك ويسمعك — يرد فقط عندما تخاطبه",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("ابدأ الجلسة")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(8.dp))
                Text("إيقاف")
            }
            Spacer(Modifier.height(24.dp))
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("💡 نصائح", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "• اضغط الفقاعة العائمة للكتم/التشغيل\n" +
                                "• اضغط مطولاً للتحدث المؤكَّد (Push-to-Talk)\n" +
                                "• فعّل كلمة الاستيقاظ من الإعدادات للتركيز الكامل\n" +
                                "• الرفيق يخفّض الجودة تلقائياً عند ضعف الشبكة أو البطارية"
                    )
                }
            }
        }
    }
}
