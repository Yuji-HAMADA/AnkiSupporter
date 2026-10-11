package com.example.ankisupporter.model

/**
 * アプリ全体の設定データ
 */
data class AppSettings(
    val activeProvider: AiProvider = AiProvider.OLLAMA,
    // Ollama 設定 (OpenAI互換 /v1/chat/completions)
    // エミュレータでは 10.0.2.2、実機では同一Wi-FiのPC IP (例: 192.168.1.100) を設定
    val ollamaHost: String = "http://10.0.2.2:11434",
    val ollamaModel: String = "llama3.2",
    // Gemini 設定 (Google AI Studio の無料APIキー)
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-1.5-flash",
    // TTS 設定 (読み上げ速度)
    val ttsSpeechRate: Float = 1.0f
)

