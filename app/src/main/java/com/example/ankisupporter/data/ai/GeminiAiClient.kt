package com.example.ankisupporter.data.ai

import android.util.Log
import com.example.ankisupporter.data.ai.dto.GeminiContent
import com.example.ankisupporter.data.ai.dto.GeminiGenerateContentRequest
import com.example.ankisupporter.data.ai.dto.GeminiGenerateContentResponse
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
 * Google AI Studio の無料枠（Free Tier）を利用したクラウドAI接続
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

    private val tag = "GeminiAiClient"
    override val provider: AiProvider = AiProvider.GEMINI

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // 429 (クォータ上限) や 503 (高負荷) 発生時の高可用性フォールバックモデル
    private val fallbackModel = "gemini-3.1-flash-lite"

    override suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Gemini APIキーが設定されていません。設定画面からGoogle AI Studioの無料キーを入力してください。")
            )
        }

        val rawModel = modelProvider().trim().ifBlank { "gemini-3.1-flash-lite" }
        val primaryModel = rawModel.removePrefix("models/").trim()

        // 1. 指定モデルで試行
        val primaryResult = executeGeneration(primaryModel, apiKey, wordOrPhrase)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        val error = primaryResult.exceptionOrNull()
        val errorMsg = error?.message ?: ""

        // 2. 503 (High demand) または 429 (Quota exceeded: 3.8-flashの1日20回制限など) の場合、軽量モデルでフォールバック
        val shouldFallback = primaryModel != fallbackModel && 
                (errorMsg.contains("503") || errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED") || errorMsg.contains("high demand"))

        if (shouldFallback) {
            Log.w(tag, "Primary model '$primaryModel' returned 429/503. Retrying with fallback model '$fallbackModel'...")
            val fallbackResult = executeGeneration(fallbackModel, apiKey, wordOrPhrase)
            if (fallbackResult.isSuccess) {
                Log.i(tag, "Fallback to '$fallbackModel' succeeded!")
                return@withContext fallbackResult
            }
        }

        primaryResult
    }

    private fun executeGeneration(model: String, apiKey: String, wordOrPhrase: String): Result<GeneratedCard> {
        return try {
            val prompt = AiPromptHelper.buildCombinedPrompt(wordOrPhrase)
            val requestBodyDto = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )

            val requestJson = AiPromptHelper.jsonConfig.encodeToString(requestBodyDto)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            Log.d(tag, "Sending generateContent to Gemini: model=$model, word=$wordOrPhrase")

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d(tag, "Gemini HTTP response: code=${response.code}, bodyLength=${responseBody.length}")

            if (!response.isSuccessful) {
                val errorDetail = parseGeminiErrorMessage(responseBody, response.code)
                Log.e(tag, "Gemini model $model failed: $errorDetail")
                return Result.failure(IOException(errorDetail))
            }

            val geminiResponse = AiPromptHelper.jsonConfig.decodeFromString<GeminiGenerateContentResponse>(responseBody)
            val parts = geminiResponse.candidates?.firstOrNull()?.content?.parts ?: emptyList()
            val contentText = parts.mapNotNull { it.text }.joinToString("\n").trim()

            if (contentText.isBlank()) {
                Log.e(tag, "Gemini returned empty text: $responseBody")
                return Result.failure(IOException("Geminiからのテキスト応答が空でした"))
            }

            val card = AiPromptHelper.parseResponseToCard(contentText, wordOrPhrase)
            Result.success(card)
        } catch (e: Exception) {
            Log.e(tag, "Exception during Gemini generation with model $model", e)
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = apiKeyProvider().trim()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Gemini APIキーが未入力です"))
            }
            val rawModel = modelProvider().trim().ifBlank { "gemini-3.1-flash-lite" }
            val cleanModel = rawModel.removePrefix("models/").trim()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .header("x-goog-api-key", apiKey)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Gemini APIへの接続に成功しました！ (モデル: $cleanModel)")
            } else {
                val errorDetail = parseGeminiErrorMessage(responseBody, response.code)
                Result.failure(IOException(errorDetail))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseGeminiErrorMessage(responseBody: String, httpCode: Int): String {
        return try {
            val geminiResponse = AiPromptHelper.jsonConfig.decodeFromString<GeminiGenerateContentResponse>(responseBody)
            val msg = geminiResponse.error?.message
            when {
                httpCode == 429 -> "HTTP 429: 無料枠の上限に達しました（${msg ?: "リクエスト制限"}）"
                httpCode == 503 -> "HTTP 503: モデルが高負荷です（${msg ?: "一時的エラー"}）"
                !msg.isNullOrBlank() -> "HTTP $httpCode: $msg"
                else -> "HTTP $httpCode: $responseBody"
            }
        } catch (e: Exception) {
            "HTTP $httpCode: $responseBody"
        }
    }
}
