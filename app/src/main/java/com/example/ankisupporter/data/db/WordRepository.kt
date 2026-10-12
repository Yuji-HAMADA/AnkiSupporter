package com.example.ankisupporter.data.db

import com.example.ankisupporter.model.GeneratedCard
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<WordEntity>>
    suspend fun getAllWordsList(): List<WordEntity>
    fun getSavedWords(): Flow<List<WordEntity>>
    fun getSearchHistory(limit: Int = 100): Flow<List<WordEntity>>
    fun getUnmemorizedSavedWords(): Flow<List<WordEntity>>
    fun getMemorizedSavedWords(): Flow<List<WordEntity>>
    fun searchSavedWords(query: String): Flow<List<WordEntity>>
    suspend fun saveCard(card: GeneratedCard): Long
    suspend fun recordSearchHistory(card: GeneratedCard): Long
    suspend fun promoteToSaved(id: Long)
    suspend fun removeFromSaved(id: Long)
    suspend fun updateWord(word: WordEntity)
    suspend fun deleteWord(word: WordEntity)
    suspend fun deleteById(id: Long)
    suspend fun toggleMemorized(id: Long, isMemorized: Boolean)
    suspend fun recordReview(id: Long)
    suspend fun importWords(words: List<WordEntity>): Int
}

class WordRepositoryImpl(private val wordDao: WordDao) : WordRepository {

    override fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    override suspend fun getAllWordsList(): List<WordEntity> = wordDao.getAllWordsList()

    override fun getSavedWords(): Flow<List<WordEntity>> = wordDao.getSavedWords()

    override fun getSearchHistory(limit: Int): Flow<List<WordEntity>> = wordDao.getSearchHistory(limit)

    override fun getUnmemorizedSavedWords(): Flow<List<WordEntity>> = wordDao.getUnmemorizedSavedWords()

    override fun getMemorizedSavedWords(): Flow<List<WordEntity>> = wordDao.getMemorizedSavedWords()

    override fun searchSavedWords(query: String): Flow<List<WordEntity>> = wordDao.searchSavedWords(query)

    override suspend fun saveCard(card: GeneratedCard): Long {
        val existing = wordDao.findWordByText(card.word.trim())
        val now = System.currentTimeMillis()
        return if (existing != null) {
            val updated = existing.copy(
                phonetic = card.phonetic,
                translation = card.translation,
                exampleSentence = card.exampleSentence,
                exampleTranslation = card.exampleTranslation,
                explanation = card.explanation,
                isSaved = true,
                savedAt = now,
                searchedAt = now
            )
            wordDao.updateWord(updated)
            existing.id
        } else {
            val newEntity = WordEntity.createAsSaved(card)
            wordDao.insertWord(newEntity)
        }
    }

    override suspend fun recordSearchHistory(card: GeneratedCard): Long {
        val entity = WordEntity.createAsHistory(card)
        return wordDao.upsertHistoryAndTrim(entity, historyLimit = 100)
    }

    override suspend fun promoteToSaved(id: Long) {
        wordDao.markAsSaved(id, System.currentTimeMillis())
    }

    override suspend fun removeFromSaved(id: Long) {
        wordDao.unmarkAsSaved(id)
    }

    override suspend fun updateWord(word: WordEntity) {
        wordDao.updateWord(word)
    }

    override suspend fun deleteWord(word: WordEntity) {
        wordDao.deleteWord(word)
    }

    override suspend fun deleteById(id: Long) {
        wordDao.deleteById(id)
    }

    override suspend fun toggleMemorized(id: Long, isMemorized: Boolean) {
        wordDao.updateMemorizedStatus(id, isMemorized)
    }

    override suspend fun recordReview(id: Long) {
        wordDao.recordReview(id)
    }

    override suspend fun importWords(words: List<WordEntity>): Int {
        if (words.isEmpty()) return 0

        val entitiesToSave = words.map { incoming ->
            val existing = wordDao.findWordByText(incoming.word.trim())
            if (existing != null) {
                incoming.copy(
                    id = existing.id,
                    isSaved = incoming.isSaved || existing.isSaved,
                    savedAt = if (incoming.isSaved || existing.isSaved) maxOf(incoming.savedAt, existing.savedAt, System.currentTimeMillis()) else 0L
                )
            } else {
                incoming.copy(id = 0)
            }
        }
        wordDao.insertWords(entitiesToSave)
        wordDao.trimUnsavedHistory(100)
        return entitiesToSave.size
    }
}
