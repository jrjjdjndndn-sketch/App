package com.aicompanion.pro.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.aicompanion.pro.data.AppDatabase
import com.aicompanion.pro.data.MessageEntity
import com.aicompanion.pro.util.Exporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var msgs by remember { mutableStateOf<List<MessageEntity>>(emptyList()) }
    val db = remember { AppDatabase.get(ctx) }

    LaunchedEffect(Unit) { msgs = db.messages().latest(200) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المحادثات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val md = Exporter.toMarkdown(msgs)
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(Intent.EXTRA_TEXT, md)
                        }
                        ctx.startActivity(Intent.createChooser(send, "تصدير المحادثة"))
                    }) { Icon(Icons.Default.Share, null) }
                    IconButton(onClick = {
                        scope.launch {
                            db.messages().clear()
                            msgs = emptyList()
                        }
                    }) { Icon(Icons.Default.DeleteSweep, null) }
                }
            )
        }
    ) { p ->
        if (msgs.isEmpty()) {
            Box(
                Modifier.padding(p).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "لا توجد محادثات بعد",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(p).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(msgs) { m ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (m.role == "user") "أنت" else "الرفيق",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(m.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
