package com.aicompanion.pro

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aicompanion.pro.service.CompanionService
import com.aicompanion.pro.ui.HistoryScreen
import com.aicompanion.pro.ui.HomeScreen
import com.aicompanion.pro.ui.MemoryScreen
import com.aicompanion.pro.ui.SettingsScreen
import com.aicompanion.pro.ui.StatsScreen
import com.aicompanion.pro.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val nav = rememberNavController()

                val perm = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { }

                LaunchedEffect(Unit) {
                    val p = mutableListOf(android.Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= 33) {
                        p += android.Manifest.permission.POST_NOTIFICATIONS
                    }
                    perm.launch(p.toTypedArray())
                }

                val media = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        val svc = Intent(this, CompanionService::class.java).apply {
                            action = CompanionService.ACTION_START
                            putExtra(CompanionService.EXTRA_CODE, result.resultCode)
                            putExtra(CompanionService.EXTRA_DATA, result.data)
                        }
                        startForegroundService(svc)
                    }
                }

                NavHost(nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onStart = {
                                if (!Settings.canDrawOverlays(this@MainActivity)) {
                                    startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:$packageName")
                                        )
                                    )
                                    return@HomeScreen
                                }
                                val m = getSystemService(MEDIA_PROJECTION_SERVICE)
                                        as MediaProjectionManager
                                media.launch(m.createScreenCaptureIntent())
                            },
                            onStop = {
                                startService(
                                    Intent(this, CompanionService::class.java)
                                        .setAction(CompanionService.ACTION_STOP)
                                )
                            },
                            onSettings = { nav.navigate("settings") },
                            onHistory = { nav.navigate("history") },
                            onStats = { nav.navigate("stats") },
                            onMemory = { nav.navigate("memory") }
                        )
                    }
                    composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
                    composable("history") { HistoryScreen(onBack = { nav.popBackStack() }) }
                    composable("stats") { StatsScreen(onBack = { nav.popBackStack() }) }
                    composable("memory") { MemoryScreen(onBack = { nav.popBackStack() }) }
                }
            }
        }
    }
}
