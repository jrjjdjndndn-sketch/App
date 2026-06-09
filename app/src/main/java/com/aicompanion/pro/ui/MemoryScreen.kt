package com.aicompanion.pro.ui

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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
import com.aicompanion.pro.data.MemoryEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.get(ctx) }
    var items by remember { mutableStateOf<List<MemoryEntity>>(emptyList()) }

    LaunchedEffect(Unit) { items = db.memory().all() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الذاكرة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            db.memory().clear()
                            items = emptyList()
                        }
                    }) { Icon(Icons.Default.DeleteSweep, null) }
                }
            )
        }
    ) { p ->
        if (items.isEmpty()) {
            Box(
                Modifier.padding(p).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "لم يحفظ الرفيق أي ذكريات بعد",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(p).fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items) { m ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                m.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(m.text, style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = {
                                scope.launch {
                                    db.memory().delete(m)
                                    items = db.memory().all()
                                }
                            }) { Icon(Icons.Default.Delete, null) }
                        }
                    }
                }
            }
        }
    }
}
