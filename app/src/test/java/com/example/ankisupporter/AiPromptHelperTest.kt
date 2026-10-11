package com.example.ankisupporter

import com.example.ankisupporter.data.ai.AiPromptHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AiPromptHelperTest {

    @Test
    fun parseResponseToCard_validJson_parsesSuccessfully() {
        val json = """
            {
              "word": "resilient",
              "phonetic": "/rɪˈzɪl.jənt/",
              "translation": "回復力のある、立ち直りの早い",
              "exampleSentence": "She is resilient and quickly bounces back from setbacks.",
              "exampleTranslation": "彼女は回復力があり、挫折からすぐに立ち直ります。",
              "explanation": "困難から回復する力があることを表すポジティブな表現です。"
            }
        """.trimIndent()

        val card = AiPromptHelper.parseResponseToCard(json, "resilient")

        assertEquals("resilient", card.word)
        assertEquals("/rɪˈzɪl.jənt/", card.phonetic)
        assertEquals("回復力のある、立ち直りの早い", card.translation)
        assertEquals("She is resilient and quickly bounces back from setbacks.", card.exampleSentence)
    }

    @Test
    fun parseResponseToCard_markdownWrappedJson_parsesSuccessfully() {
        val markdownJson = """
            Here is the result:
            ```json
            {
              "word": "persistent",
              "phonetic": "/pərˈsɪs.tənt/",
              "translation": "粘り強い、持続的な",
              "exampleSentence": "He was persistent in pursuing his dreams.",
              "exampleTranslation": "彼は夢を追い求めることに粘り強かった。",
              "explanation": "あきらめずに続ける態度を指します。"
            }
            ```
            Hope this helps!
        """.trimIndent()

        val card = AiPromptHelper.parseResponseToCard(markdownJson, "persistent")

        assertEquals("persistent", card.word)
        assertEquals("粘り強い、持続的な", card.translation)
    }

    @Test
    fun parseResponseToCard_invalidJson_returnsFallback() {
        val invalidText = "Plain text response without json"
        val card = AiPromptHelper.parseResponseToCard(invalidText, "fallbackWord")

        assertEquals("fallbackWord", card.word)
        assertNotNull(card.explanation)
    }
}

