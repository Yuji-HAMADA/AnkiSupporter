package com.example.ankisupporter.model

/**
 * 接続先AIプロバイダーの種別
 */
enum class AiProvider(val displayName: String) {
    OLLAMA("ローカルLLM (Ollama)"),
    GEMINI("クラウド無料AI (Google Gemini)")
}

