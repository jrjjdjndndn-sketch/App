package com.aicompanion.pro.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ClientSetup(val setup: Setup)

@Serializable
data class Setup(
    val model: String,
    @SerialName("generation_config") val generationConfig: GenConfig,
    @SerialName("system_instruction") val systemInstruction: SystemInstruction,
    @SerialName("realtime_input_config") val realtimeInputConfig: RealtimeInputConfig? = null,
    val tools: List<ToolDecl>? = null
)

@Serializable
data class GenConfig(
    @SerialName("response_modalities") val responseModalities: List<String> = listOf("AUDIO"),
    @SerialName("speech_config") val speechConfig: SpeechConfig? = null,
    val temperature: Float = 0.8f
)

@Serializable
data class SpeechConfig(
    @SerialName("voice_config") val voiceConfig: VoiceConfig,
    @SerialName("language_code") val languageCode: String = "ar-XA"
)

@Serializable
data class VoiceConfig(
    @SerialName("prebuilt_voice_config") val prebuilt: PrebuiltVoice
)

@Serializable
data class PrebuiltVoice(
    @SerialName("voice_name") val voiceName: String = "Puck"
)

@Serializable
data class SystemInstruction(val parts: List<TextPart>)

@Serializable
data class TextPart(val text: String)

@Serializable
data class RealtimeInputConfig(
    @SerialName("automatic_activity_detection") val autoVad: AutoVad = AutoVad()
)

@Serializable
data class AutoVad(val disabled: Boolean = true)

@Serializable
data class ToolDecl(
    @SerialName("function_declarations") val functionDeclarations: List<FunctionDecl>
)

@Serializable
data class FunctionDecl(
    val name: String,
    val description: String,
    val parameters: JsonElement
)

@Serializable
data class ClientRealtimeInput(
    @SerialName("realtime_input") val realtimeInput: RealtimeInput
)

@Serializable
data class RealtimeInput(
    @SerialName("media_chunks") val mediaChunks: List<MediaChunk>
)

@Serializable
data class MediaChunk(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class ClientToolResponse(
    @SerialName("tool_response") val toolResponse: ToolResponse
)

@Serializable
data class ToolResponse(
    @SerialName("function_responses") val functionResponses: List<FunctionResp>
)

@Serializable
data class FunctionResp(
    val id: String,
    val name: String,
    val response: JsonElement
)

@Serializable
data class ServerMessage(
    @SerialName("server_content") val serverContent: ServerContent? = null,
    @SerialName("tool_call") val toolCall: ToolCall? = null,
    @SerialName("setup_complete") val setupComplete: JsonElement? = null,
    @SerialName("usage_metadata") val usageMetadata: UsageMeta? = null
)

@Serializable
data class ServerContent(
    @SerialName("model_turn") val modelTurn: ModelTurn? = null,
    @SerialName("turn_complete") val turnComplete: Boolean? = null,
    val interrupted: Boolean? = null
)

@Serializable
data class ModelTurn(val parts: List<ModelPart>)

@Serializable
data class ModelPart(
    @SerialName("inline_data") val inlineData: InlineData? = null,
    val text: String? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class ToolCall(
    @SerialName("function_calls") val functionCalls: List<FunctionCall>
)

@Serializable
data class FunctionCall(
    val id: String,
    val name: String,
    val args: JsonElement
)

@Serializable
data class UsageMeta(
    @SerialName("total_token_count") val totalTokenCount: Int? = null,
    @SerialName("prompt_token_count") val promptTokenCount: Int? = null,
    @SerialName("response_token_count") val responseTokenCount: Int? = null
)
