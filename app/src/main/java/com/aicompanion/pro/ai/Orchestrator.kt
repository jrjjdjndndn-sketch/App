package com.aicompanion.pro.ai

import com.aicompanion.pro.audio.AudioPlayer
import com.aicompanion.pro.audio.MicCapturer
import com.aicompanion.pro.audio.SileroVad
import com.aicompanion.pro.audio.WakeWordGate
import com.aicompanion.pro.data.AppDatabase
import com.aicompanion.pro.data.UsageEntity
import com.aicompanion.pro.service.ScreenCapturer
import com.aicompanion.pro.util.BatteryMonitor
import com.aicompanion.pro.util.ImageEncoder
import com.aicompanion.pro.util.NetworkMonitor
import com.aicompanion.pro.util.PerceptualHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

enum class CompanionState { IDLE, LISTENING, THINKING, SPEAKING, MUTED, RECONNECTING }

class Orchestrator(
    private val scope: CoroutineScope,
    private val mic: MicCapturer,
    private val vad: SileroVad,
    private val screen: ScreenCapturer,
    private val player: AudioPlayer,
    private val sessionFactory: suspend () -> GeminiLiveSession,
    private val systemPrompt: String,
    private val tools: ToolRegistry,
    private val db: AppDatabase,
    private val wakeWord: WakeWordGate,
    private val network: NetworkMonitor,
    private val battery: BatteryMonitor
) {
    private val _state = MutableStateFlow(CompanionState.IDLE)
    val state: StateFlow<CompanionState> = _state

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted

    private val _tokensTotal = MutableStateFlow(0)
    val tokensTotal: StateFlow<Int> = _tokensTotal

    private val preroll = ArrayDeque<ByteArray>()
    private val prerollLimit = 60

    private var inUtterance = false
    private var silenceFrames = 0
    private val endSilenceFrames = 25

    private var session: GeminiLiveSession? = null
    private var lastFrameAt = 0L
    private var lastHash = 0L

    fun start() {
        scope.launch { sessionLoop() }
        scope.launch { handleMic() }
    }

    fun toggleMute() {
        _muted.value = !_muted.value
        _state.value = if (_muted.value) CompanionState.MUTED else CompanionState.IDLE
    }

    private suspend fun sessionLoop() {
        while (scope.isActive) {
            try {
                _state.value = CompanionState.RECONNECTING
                val s = sessionFactory()
                s.connect(systemPrompt, tools.declarations())
                session = s
                wireSession(s)
                _state.value = if (_muted.value) CompanionState.MUTED else CompanionState.IDLE
                s.connected.collect { connected ->
                    if (!connected) throw RuntimeException("disconnected")
                }
            } catch (_: Exception) {
                delay(3000)
            }
        }
    }

    private fun wireSession(s: GeminiLiveSession) {
        scope.launch {
            s.inboundAudio.collect {
                _state.value = CompanionState.SPEAKING
                player.enqueue(it)
            }
        }
        scope.launch {
            s.turnComplete.collect {
                _state.value = if (_muted.value) CompanionState.MUTED else CompanionState.IDLE
            }
        }
        scope.launch {
            s.interrupted.collect { player.flush() }
        }
        scope.launch {
            s.toolCalls.collect { call ->
                val args = call.args as? JsonObject ?: JsonObject(emptyMap())
                val result = tools.invoke(call.name, args)
                s.sendToolResponse(call.id, call.name, result)
            }
        }
        scope.launch {
            s.tokenUsage.collect { u ->
                u.totalTokenCount?.let { total ->
                    _tokensTotal.value += total
                    db.usage().insert(UsageEntity(tokens = total))
                }
            }
        }
    }

    private suspend fun handleMic() {
        mic.stream().collect { frame ->
            if (_muted.value) return@collect
            val isSpeech = vad.isSpeech(frame.floats)

            preroll.addLast(frame.pcm16le)
            if (preroll.size > prerollLimit) preroll.removeFirst()

            if (player.speaking.value && isSpeech) player.flush()

            when {
                isSpeech && !inUtterance -> {
                    if (!wakeWord.isArmed()) return@collect
                    inUtterance = true
                    silenceFrames = 0
                    _state.value = CompanionState.LISTENING
                    val s = session ?: return@collect
                    for (b in preroll) s.sendAudio(b)
                    sendScreenSmart(force = true)
                }
                isSpeech && inUtterance -> {
                    silenceFrames = 0
                    session?.sendAudio(frame.pcm16le)
                    sendScreenSmart()
                }
                !isSpeech && inUtterance -> {
                    silenceFrames++
                    session?.sendAudio(frame.pcm16le)
                    if (silenceFrames >= endSilenceFrames) {
                        inUtterance = false
                        _state.value = CompanionState.THINKING
                    }
                }
                else -> { }
            }
        }
    }

    private suspend fun sendScreenSmart(force: Boolean = false) {
        val now = System.currentTimeMillis()
        val intervalMs = computeFrameInterval()
        if (!force && now - lastFrameAt < intervalMs) return
        val bmp = screen.captureLatest() ?: return
        val hash = PerceptualHash.dhash(bmp)
        if (!force && PerceptualHash.hamming(hash, lastHash) < 6) return
        lastHash = hash
        lastFrameAt = now
        val (q, w) = computeQualityAndWidth()
        val webp = ImageEncoder.toWebp(bmp, q, w)
        session?.sendImage(webp)
    }

    private fun computeFrameInterval(): Long {
        val base = 1500L
        val low = battery.isLowBattery()
        val slow = network.isSlow()
        return when {
            low && slow -> base * 3
            low || slow -> base * 2
            else -> base
        }
    }

    private fun computeQualityAndWidth(): Pair<Int, Int> {
        val slow = network.isSlow()
        return if (slow) 50 to 720 else 65 to 1024
    }
}
