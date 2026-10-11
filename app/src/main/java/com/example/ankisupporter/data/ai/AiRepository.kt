package com.example.ankisupporter.data.ai

import com.example.ankisupporter.data.preferences.AppPreferences
import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.model.AppSettings
import com.example.ankisupporter.model.GeneratedCard
import kotlinx.coroutines.flow.first

/**
 * AI生成のRepositoryインターフェース
 */
interface AiRepository {
    suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard>
    suspend fun testConnection(provider: AiProvider): Result<String>
}

/**
 * 現在の設定に応じて Ollama / Gemini クライアントへ動的にルーティングする実装
 */
class AiRepositoryImpl(
    private val preferences: AppPreferences
) : AiRepository {

    // キャッシュされた設定（随時Flowから取得）
    private suspend fun getSettings(): AppSettings = preferences.settingsFlow.first()

    private val ollamaClient = OllamaAiClient(
        hostUrlProvider = {
            // preferences から取得するためブロック的に実行可能にするか、最新設定を保持
            currentSettings.ollamaHost
        },
        modelProvider = {
            currentSettings.ollamaModel
        }
    )

    private val geminiClient = GeminiAiClient(
        apiKeyProvider = {
            currentSettings.geminiApiKey
        },
        modelProvider = {
            currentSettings.geminiModel
        }
    )

    @Volatile
    private var currentSettings: AppSettings = AppSettings()

    suspend fun syncSettings() {
        currentSettings = getSettings()
    }

    override suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard> {
        syncSettings()
        val client = when (currentSettings.activeProvider) {
            AiProvider.OLLAMA -> ollamaClient
            AiProvider.GEMINI -> geminiClient
        }
        return client.generateExample(wordOrPhrase)
    }

    override suspend fun testConnection(provider: AiProvider): Result<String> {
        syncSettings()
        val client = when (provider) {
            AiProvider.OLLAMA -> ollamaClient
            AiProvider.GEMINI -> geminiClient
        }
        return client.testConnection()
    }
}

