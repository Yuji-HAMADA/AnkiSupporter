package com.example.ankisupporter.data.ai

import com.example.ankisupporter.model.GeneratedCard
import kotlinx.serialization.json.Json

object AiPromptHelper {

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * あらゆるLLM（Ollama, Gemini等）で最も安定してJSONを出力させる統合プロンプト
     */
    fun buildCombinedPrompt(input: String): String {
        return """
You are an expert English learning assistant specializing in Japanese university entrance examinations.
Create an English study flashcard for the following word/text: "$input"

Provide exactly 3 English synonyms or closely related words suitable for Japanese university entrance examination level (standard to difficult university exams, approximately CEFR B1-B2 level).

You must reply with ONLY a single valid JSON object, without any introductory or concluding text.
Format requirements:
{
  "word": "The English word or phrase",
  "phonetic": "IPA transcription, e.g. /æp.əl/",
  "translation": "Accurate Japanese meaning",
  "exampleSentence": "A natural, practical English example sentence",
  "exampleTranslation": "Japanese translation of the example sentence",
  "explanation": "Brief explanation of usage, collocation, or nuance in Japanese",
  "synonyms": ["synonym1", "synonym2", "synonym3"]
}
""".trimIndent()
    }

    /**
     * LLMの出力テキストからJSONを抽出してGeneratedCardに変換
     */
    fun parseResponseToCard(rawText: String, fallbackWord: String): GeneratedCard {
        val trimmed = rawText.trim()

        // 1. ```json { ... } ``` の抽出
        val jsonPattern = Regex("""```(?:json)?\s*(\{[\s\S]*?\})\s*```""", RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonPattern.find(trimmed)
        val jsonCandidate = if (jsonMatch != null) {
            jsonMatch.groupValues[1]
        } else {
            // 2. 文字列内の最初と最後の波括弧を探す
            val firstBrace = trimmed.indexOf('{')
            val lastBrace = trimmed.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace < trimmed.length && lastBrace > firstBrace) {
                trimmed.substring(firstBrace, lastBrace + 1)
            } else {
                trimmed
            }
        }

        return try {
            val card = jsonConfig.decodeFromString<GeneratedCard>(jsonCandidate)
            card.copy(
                word = if (card.word.isNotBlank()) card.word else fallbackWord,
                translation = if (card.translation.isNotBlank()) card.translation else fallbackWord,
                synonyms = card.synonyms.filter { it.isNotBlank() }
            )
        } catch (e: Exception) {
            // パース失敗時のフォールバックカード
            GeneratedCard(
                word = fallbackWord,
                phonetic = "",
                translation = "（返答のパースに失敗しました）",
                exampleSentence = trimmed.take(200),
                exampleTranslation = "",
                explanation = "AI返答のJSON抽出に失敗しました: ${e.localizedMessage ?: "フォーマットエラー"}",
                synonyms = emptyList()
            )
        }
    }
}
