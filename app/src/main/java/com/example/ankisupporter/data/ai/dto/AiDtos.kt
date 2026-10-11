package com.example.ankisupporter.data.ai.dto

import kotlinx.serialization.Serializable

// --- OpenAI / Ollama 互換 API DTO ---
@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    val stream: Boolean = false
)

@Serializable
data class OpenAiMessage(
    val role: String, // "system" or "user" or "assistant"
    val content: String
)

@Serializable
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage? = null
)

@Serializable
data class OpenAiError(
    val message: String? = null
)

// --- Google Gemini API DTO ---
@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val responseMimeType: String? = null
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

