package com.aicompanion.pro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.aicompanion.pro.MainActivity
import com.aicompanion.pro.ai.GeminiLiveSession
import com.aicompanion.pro.ai.Orchestrator
import com.aicompanion.pro.ai.SystemPrompt
import com.aicompanion.pro.ai.ToolRegistry
import com.aicompanion.pro.audio.AudioPlayer
import com.aicompanion.pro.audio.MicCapturer
import com.aicompanion.pro.audio.SileroVad
import com.aicompanion.pro.audio.WakeWordGate
import com.aicompanion.pro.data.AppDatabase
import com.aicompanion.pro.data.SettingsStore
import com.aicompanion.pro.util.BatteryMonitor
import com.aicompanion.pro.util.ForegroundAppDetector
import com.aicompanion.pro.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CompanionService : LifecycleService() {

    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_MUTE = "MUTE"
        const val EXTRA_CODE = "code"
        const val EXTRA_DATA = "data"
        private const val CHANNEL = "companion"
        private const val NID = 9090
    }

    private lateinit var screen: ScreenCapturer
    private lateinit var player: AudioPlayer
    private lateinit var settings: SettingsStore
    private lateinit var db: AppDatabase
    private var overlay: OverlayManager? = null
    private var orchestrator: Orchestrator? = null
    private var vad: SileroVad? = null
    private var network: NetworkMonitor? = null
    private var battery: BatteryMonitor? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        screen = ScreenCapturer(this)
        player = AudioPlayer()
        settings = SettingsStore(this)
        db = AppDatabase.get(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> bootstrap(intent)
            ACTION_MUTE -> orchestrator?.toggleMute()
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun bootstrap(intent: Intent) {
        val code = intent.getIntExtra(EXTRA_CODE, 0)
        val data: Intent = intent.getParcelableExtra(EXTRA_DATA) ?: return
        startInForeground()
        screen.start(code, data)
        vad = SileroVad(this)
        network = NetworkMonitor(this).also { it.start() }
        battery = BatteryMonitor(this).also { it.start() }

        lifecycleScope.launch {
            val key = settings.apiKey.first()
            if (key.isBlank()) {
                stopSelf()
                return@launch
            }
            val voice = settings.voice.first()
            val lang = settings.language.first()
            val name = settings.userName.first()
            val requireWake = settings.requireWakeWord.first()
            val proactive = settings.proactive.first()

            val currentApp = ForegroundAppDetector.detect(this@CompanionService)
            val memories = db.memory().all().map { it.text }
            val prompt = SystemPrompt.build(
                userName = name,
                currentApp = currentApp,
                recentSummary = null,
                memories = memories,
                wakeWord = if (requireWake) "يا رفيق" else null,
                proactive = proactive
            )

            val tools = ToolRegistry(applicationContext, db)
            val gate = WakeWordGate(requireWake)
            val mic = MicCapturer()

            orchestrator = Orchestrator(
                scope = lifecycleScope,
                mic = mic,
                vad = vad!!,
                screen = screen,
                player = player,
                sessionFactory = {
                    GeminiLiveSession(key, voice = voice, language = lang)
                },
                systemPrompt = prompt,
                tools = tools,
                db = db,
                wakeWord = gate,
                network = network!!,
                battery = battery!!
            ).also { it.start() }

            overlay = OverlayManager(
                ctx = this@CompanionService,
                scope = lifecycleScope,
                state = orchestrator!!.state,
                muted = orchestrator!!.muted,
                onTap = { orchestrator?.toggleMute() },
                onLongPressStart = { gate.arm(15_000) },
                onLongPressEnd = { }
            ).also { it.show() }
        }
    }

    private fun startInForeground() {
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val mute = PendingIntent.getService(
            this, 1,
            Intent(this, CompanionService::class.java).setAction(ACTION_MUTE),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 2,
            Intent(this, CompanionService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val n = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("AI Companion Pro")
            .setContentText("صامت — كلّمني وأنا أرد")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(open)
            .addAction(0, "كتم", mute)
            .addAction(0, "إيقاف", stop)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NID, n,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NID, n)
        }
    }

    private fun createChannel() {
        val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (m.getNotificationChannel(CHANNEL) == null) {
            m.createNotificationChannel(
                NotificationChannel(
                    CHANNEL, "Companion",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun onDestroy() {
        runCatching { overlay?.hide() }
        runCatching { vad?.close() }
        runCatching { screen.stop() }
        runCatching { player.release() }
        runCatching { network?.stop() }
        runCatching { battery?.stop() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
