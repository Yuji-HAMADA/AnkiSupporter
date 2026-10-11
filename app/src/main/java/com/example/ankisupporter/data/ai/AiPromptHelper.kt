package com.example.ankisupporter.data.ai

import com.example.ankisupporter.model.GeneratedCard
import kotlinx.serialization.json.Json

object AiPromptHelper {

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    const val SYSTEM_PROMPT = """
You are a helpful and expert English learning assistant.
When the user gives you an English or Japanese word, phrase, or sentence, you must generate:
1. The English word/phrase.
2. IPA phonetic transcription (e.g. /ɪɡˈzæmpəl/).
3. Accurate Japanese translation of the word.
4. A natural, practical English example sentence using the word (B1-B2 level, useful for daily life or business).
5. The Japanese translation of that example sentence.
6. A concise learning tip, nuance, or collocation in Japanese.

You MUST respond strictly with a valid JSON object in the following format and NOTHING ELSE:
{
  "word": "example",
  "phonetic": "/ɪɡˈzæmpəl/",
  "translation": "例、見本、手本",
  "exampleSentence": "Can you give me an example of how this word is used?",
  "exampleTranslation": "この単語がどのように使われるか例を挙げていただけますか？",
  "explanation": "「give an example」などのコロケーションでよく使われます。"
}
"""

    fun buildUserPrompt(input: String): String {
        return "Word or text to learn: $input"
    }

    /**
     * LLMの出力テキストからJSONを抽出してGeneratedCardに変換
     */
    fun parseResponseToCard(rawText: String, fallbackWord: String): GeneratedCard {
        val trimmed = rawText.trim()
        
        // コードブロック ```json ... ``` の除去
        val jsonPattern = Regex("""```(?:json)?\s*(\{[\s\S]*?\})\s*```""", RegexOption.DOT_MATCHES_ALL)
        val jsonMatch = jsonPattern.find(trimmed)
        val jsonCandidate = if (jsonMatch != null) {
            jsonMatch.groupValues[1]
        } else {
            // 文字列内の最初と最後の波括弧を探す
            val firstBrace = trimmed.indexOf('{')
            val lastBrace = trimmed.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                trimmed.substring(firstBrace, lastBrace + 1)
            } else {
                trimmed
            }
        }

        return try {
            jsonConfig.decodeFromString<GeneratedCard>(jsonCandidate)
        } catch (e: Exception) {
            // パース失敗時のフォールバック処理
            GeneratedCard(
                word = fallbackWord,
                phonetic = "",
                translation = "（パースエラー: レスポンスをそのまま表示します）",
                exampleSentence = rawText,
                exampleTranslation = "",
                explanation = "AIからの返答形式が不正でした: ${e.localizedMessage}"
            )
        }
    }
}

