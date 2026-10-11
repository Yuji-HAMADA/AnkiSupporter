package com.example.ankisupporter.data.db

import com.example.ankisupporter.model.GeneratedCard
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<WordEntity>>
    fun getUnmemorizedWords(): Flow<List<WordEntity>>
    fun getMemorizedWords(): Flow<List<WordEntity>>
    fun searchWords(query: String): Flow<List<WordEntity>>
    suspend fun saveCard(card: GeneratedCard): Long
    suspend fun updateWord(word: WordEntity)
    suspend fun deleteWord(word: WordEntity)
    suspend fun deleteById(id: Long)
    suspend fun toggleMemorized(id: Long, isMemorized: Boolean)
    suspend fun recordReview(id: Long)
}

class WordRepositoryImpl(private val wordDao: WordDao) : WordRepository {

    override fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    override fun getUnmemorizedWords(): Flow<List<WordEntity>> = wordDao.getUnmemorizedWords()

    override fun getMemorizedWords(): Flow<List<WordEntity>> = wordDao.getMemorizedWords()

    override fun searchWords(query: String): Flow<List<WordEntity>> = wordDao.searchWords(query)

    override suspend fun saveCard(card: GeneratedCard): Long {
        val entity = WordEntity.fromGeneratedCard(card)
        return wordDao.insertWord(entity)
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
}

