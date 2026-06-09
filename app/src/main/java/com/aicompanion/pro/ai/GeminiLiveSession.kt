package com.aicompanion.pro.ai

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GeminiLiveSession(
    private val apiKey: String,
    private val model: String = "gemini-3.5-flash",
    private val voice: String = "Puck",
    private val language: String = "ar-XA"
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val client = HttpClient(OkHttp) {
        install(WebSockets) { pingIntervalMillis = 20_000 }
    }

    private var session: DefaultWebSocketSession? = null
    private val outbound = Channel<String>(Channel.BUFFERED)

    private val _inboundAudio = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val inboundAudio: SharedFlow<ByteArray> = _inboundAudio

    private val _turnComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val turnComplete: SharedFlow<Unit> = _turnComplete

    private val _interrupted = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val interrupted: SharedFlow<Unit> = _interrupted

    private val _toolCalls = MutableSharedFlow<FunctionCall>(extraBufferCapacity = 8)
    val toolCalls: SharedFlow<FunctionCall> = _toolCalls

    private val _tokenUsage = MutableSharedFlow<UsageMeta>(extraBufferCapacity = 8)
    val tokenUsage: SharedFlow<UsageMeta> = _tokenUsage

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun connect(systemInstruction: String, tools: List<ToolDecl> = emptyList()) {
        val url = "wss://generativelanguage.googleapis.com/ws/" +
                "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent" +
                "?key=$apiKey"

        session = client.webSocketSession { url(url) }
        val setup = ClientSetup(
            Setup(
                model = "models/$model",
                generationConfig = GenConfig(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = SpeechConfig(VoiceConfig(PrebuiltVoice(voice)), language)
                ),
                systemInstruction = SystemInstruction(listOf(TextPart(systemInstruction))),
                realtimeInputConfig = RealtimeInputConfig(AutoVad(disabled = true)),
                tools = tools.ifEmpty { null }
            )
        )
        session!!.send(Frame.Text(json.encodeToString(setup)))
        _connected.value = true

        scope.launch {
            try {
                for (msg in outbound) session?.send(Frame.Text(msg))
            } catch (_: Exception) { }
        }
        scope.launch { receiveLoop() }
    }

    private suspend fun receiveLoop() {
        val ws = session ?: return
        try {
            for (frame in ws.incoming) {
                if (frame !is Frame.Text) continue
                val msg = runCatching {
                    json.decodeFromString<ServerMessage>(frame.readText())
                }.getOrNull() ?: continue

                msg.serverContent?.modelTurn?.parts?.forEach { part ->
                    part.inlineData?.let { inline ->
                        if (inline.mimeType.startsWith("audio/")) {
                            _inboundAudio.emit(Base64.decode(inline.data, Base64.NO_WRAP))
                        }
                    }
                }
                msg.toolCall?.functionCalls?.forEach { _toolCalls.emit(it) }
                msg.usageMetadata?.let { _tokenUsage.emit(it) }
                if (msg.serverContent?.interrupted == true) _interrupted.emit(Unit)
                if (msg.serverContent?.turnComplete == true) _turnComplete.emit(Unit)
            }
        } catch (_: Exception) {
            _connected.value = false
        }
    }

    suspend fun sendAudio(pcm16le16k: ByteArray) {
        val b64 = Base64.encodeToString(pcm16le16k, Base64.NO_WRAP)
        outbound.send(
            json.encodeToString(
                ClientRealtimeInput(
                    RealtimeInput(listOf(MediaChunk("audio/pcm;rate=16000", b64)))
                )
            )
        )
    }

    suspend fun sendImage(webp: ByteArray) {
        val b64 = Base64.encodeToString(webp, Base64.NO_WRAP)
        outbound.send(
            json.encodeToString(
                ClientRealtimeInput(
                    RealtimeInput(listOf(MediaChunk("image/webp", b64)))
                )
            )
        )
    }

    suspend fun sendToolResponse(
        id: String,
        name: String,
        response: kotlinx.serialization.json.JsonObject
    ) {
        outbound.send(
            json.encodeToString(
                ClientToolResponse(
                    ToolResponse(listOf(FunctionResp(id, name, response)))
                )
            )
        )
    }

    suspend fun close() {
        _connected.value = false
        runCatching { session?.close() }
        outbound.close()
        scope.cancel()
        client.close()
    }
}
