package com.example.ankisupporter.model

import com.example.ankisupporter.BuildConfig

/**
 * アプリ全体の設定データ
 */
data class AppSettings(
    val activeProvider: AiProvider = AiProvider.OLLAMA,
    // Ollama 設定 (OpenAI互換 /v1/chat/completions)
    val ollamaHost: String = "http://10.0.2.2:11434",
    val ollamaModel: String = "llama3.2",
    // Gemini 設定 (local.properties または Google AI Studio の無料APIキー)
    val geminiApiKey: String = BuildConfig.DEFAULT_GEMINI_API_KEY,
    val geminiModel: String = "gemini-3.1-flash-lite",
    // TTS 設定 (読み上げ速度)
    val ttsSpeechRate: Float = 1.0f
)
