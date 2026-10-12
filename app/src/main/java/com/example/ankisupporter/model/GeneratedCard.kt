package com.example.ankisupporter.model

import kotlinx.serialization.Serializable

/**
 * AIによって生成された英語学習カード情報
 */
@Serializable
data class GeneratedCard(
    val word: String,
    val phonetic: String = "",
    val translation: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val explanation: String = "",
    val synonyms: List<String> = emptyList()
)
