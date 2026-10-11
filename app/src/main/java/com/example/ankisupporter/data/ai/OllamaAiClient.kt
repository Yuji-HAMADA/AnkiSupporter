package com.example.ankisupporter.data.ai

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

    override val provider: AiProvider = AiProvider.OLLAMA

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard> = withContext(Dispatchers.IO) {
        try {
            val host = hostUrlProvider().trim().trimEnd('/')
            val model = modelProvider().trim().ifBlank { "llama3.2" }

            val requestBodyDto = OpenAiChatRequest(
                model = model,
                messages = listOf(
                    OpenAiMessage(role = "system", content = AiPromptHelper.SYSTEM_PROMPT),
                    OpenAiMessage(role = "user", content = AiPromptHelper.buildUserPrompt(wordOrPhrase))
                ),
                temperature = 0.3f,
                stream = false
            )

            val requestJson = AiPromptHelper.jsonConfig.encodeToString(requestBodyDto)
            val url = "$host/v1/chat/completions"

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Ollama HTTPエラー ${response.code}: $responseBody")
                )
            }

            val chatResponse = AiPromptHelper.jsonConfig.decodeFromString<OpenAiChatResponse>(responseBody)
            val content = chatResponse.choices?.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(IOException("Ollamaからの応答が空でした"))

            val card = AiPromptHelper.parseResponseToCard(content, wordOrPhrase)
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val host = hostUrlProvider().trim().trimEnd('/')
            val request = Request.Builder()
                .url("$host/v1/models")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("ローカルLLMへの接続に成功しました！ (HTTP ${response.code})")
            } else {
                Result.failure(IOException("HTTP ${response.code}: ${response.body?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

