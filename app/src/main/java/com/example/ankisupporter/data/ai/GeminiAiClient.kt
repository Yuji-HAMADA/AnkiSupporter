package com.example.ankisupporter.data.ai

import com.example.ankisupporter.data.ai.dto.GeminiContent
import com.example.ankisupporter.data.ai.dto.GeminiGenerateContentRequest
import com.example.ankisupporter.data.ai.dto.GeminiGenerateContentResponse
import com.example.ankisupporter.data.ai.dto.GeminiGenerationConfig
import com.example.ankisupporter.data.ai.dto.GeminiPart
import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.model.GeneratedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Google Gemini API クライアント
 * Google AI Studio の無料枠（Free Tier）を利用した完全無料のクラウドAI接続
 */
class GeminiAiClient(
    private val apiKeyProvider: () -> String,
    private val modelProvider: () -> String,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiClient {

    override val provider: AiProvider = AiProvider.GEMINI

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiKeyProvider().trim()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Gemini APIキーが設定されていません。設定画面からGoogle AI Studioの無料キーを入力してください。")
                )
            }

            val model = modelProvider().trim().ifBlank { "gemini-1.5-flash" }

            val requestBodyDto = GeminiGenerateContentRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = AiPromptHelper.SYSTEM_PROMPT))
                ),
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = AiPromptHelper.buildUserPrompt(wordOrPhrase)))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    responseMimeType = "application/json"
                )
            )

            val requestJson = AiPromptHelper.jsonConfig.encodeToString(requestBodyDto)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Gemini APIエラー HTTP ${response.code}: $responseBody")
                )
            }

            val geminiResponse = AiPromptHelper.jsonConfig.decodeFromString<GeminiGenerateContentResponse>(responseBody)
            val contentText = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(IOException("Geminiからのテキスト応答が空でした"))

            val card = AiPromptHelper.parseResponseToCard(contentText, wordOrPhrase)
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiKeyProvider().trim()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Gemini APIキーが未入力です"))
            }
            val model = modelProvider().trim().ifBlank { "gemini-1.5-flash" }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Gemini APIへの接続に成功しました！ (モデル: $model)")
            } else {
                Result.failure(IOException("HTTP ${response.code}: ${response.body?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

