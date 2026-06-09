package com.aicompanion.pro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicompanion.pro.data.AppDatabase
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    var today by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var week by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val now = Calendar.getInstance()
        val dayStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekStart = dayStart - 6 * 24L * 60 * 60 * 1000
        today = db.usage().sumSince(dayStart)
        week = db.usage().sumSince(weekStart)
        total = db.usage().total()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإحصائيات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        Column(
            Modifier.padding(p).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("توكنز اليوم", today.toString())
            StatCard("توكنز آخر 7 أيام", week.toString())
            StatCard("الإجمالي الكلي", total.toString())
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}
