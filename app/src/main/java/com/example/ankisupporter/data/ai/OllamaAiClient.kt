package com.example.ankisupporter.data.ai

import android.util.Log
import com.example.ankisupporter.data.ai.dto.OpenAiChatRequest
import com.example.ankisupporter.data.ai.dto.OpenAiChatResponse
import com.example.ankisupporter.data.ai.dto.OpenAiMessage
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
 * ローカルLLM (Ollama等) の OpenAI互換APIクライアント
 * 完全無料・ローカル完結・APIキー不要
 */
class OllamaAiClient(
    private val hostUrlProvider: () -> String,
    private val modelProvider: () -> String,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiClient {

    private val tag = "OllamaAiClient"
    override val provider: AiProvider = AiProvider.OLLAMA

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * モデル名表記の補正 (例: gemma2.9b -> gemma2:9b)
     */
    private fun normalizeModelName(rawModel: String): String {
        val trimmed = rawModel.trim()
        // タグの区切りがドットになっている場合 (例: gemma2.9b, llama3.8b) はコロンに正規化
        return if (!trimmed.contains(":") && trimmed.matches(Regex("^[a-zA-Z0-9_-]+\\.[0-9]+[a-zA-Z]?.*$"))) {
            trimmed.replaceFirst(".", ":")
        } else {
            trimmed
        }
    }

    override suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard> = withContext(Dispatchers.IO) {
        try {
            val host = hostUrlProvider().trim().trimEnd('/')
            val rawModel = modelProvider().trim().ifBlank { "gemma2:9b" }
            val model = normalizeModelName(rawModel)

            val prompt = AiPromptHelper.buildCombinedPrompt(wordOrPhrase)
            val requestBodyDto = OpenAiChatRequest(
                model = model,
                messages = listOf(
                    OpenAiMessage(role = "user", content = prompt)
                ),
                temperature = 0.3f,
                stream = false
            )

            val requestJson = AiPromptHelper.jsonConfig.encodeToString(requestBodyDto)
            val url = "$host/v1/chat/completions"

            Log.d(tag, "Sending request to Ollama: url=$url, model=$model, word=$wordOrPhrase")

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d(tag, "Ollama HTTP code=${response.code}, bodyLength=${responseBody.length}")

            if (!response.isSuccessful) {
                Log.e(tag, "Ollama request failed HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(
                    IOException("Ollama HTTPエラー ${response.code}: $responseBody")
                )
            }

            val chatResponse = AiPromptHelper.jsonConfig.decodeFromString<OpenAiChatResponse>(responseBody)
            val content = chatResponse.choices?.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(IOException("Ollamaからの応答テキストが空でした"))

            val card = AiPromptHelper.parseResponseToCard(content, wordOrPhrase)
            Result.success(card)
        } catch (e: Exception) {
            Log.e(tag, "Ollama generation exception", e)
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val host = hostUrlProvider().trim().trimEnd('/')
            val rawModel = modelProvider().trim()
            val normalizedModel = normalizeModelName(rawModel)

            val request = Request.Builder()
                .url("$host/v1/models")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val modelNote = if (rawModel != normalizedModel) {
                    "\n(ヒント: モデル名を '$rawModel' から '$normalizedModel' に自動補正しました)"
                } else ""
                Result.success("ローカルLLMへの接続に成功しました！ (HTTP 200)$modelNote")
            } else {
                Result.failure(IOException("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
