package com.example.ankisupporter.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ankisupporter.model.GeneratedCard

/**
 * 暗記・復習用の単語帳エンティティ
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val translation: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val explanation: String = "",
    val isMemorized: Boolean = false,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long = 0L
) {
    fun toGeneratedCard(): GeneratedCard {
        return GeneratedCard(
            word = word,
            phonetic = phonetic,
            translation = translation,
            exampleSentence = exampleSentence,
            exampleTranslation = exampleTranslation,
            explanation = explanation
        )
    }

    companion object {
        fun fromGeneratedCard(card: GeneratedCard): WordEntity {
            return WordEntity(
                word = card.word,
                phonetic = card.phonetic,
                translation = card.translation,
                exampleSentence = card.exampleSentence,
                exampleTranslation = card.exampleTranslation,
                explanation = card.explanation
            )
        }
    }
}

