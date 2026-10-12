package com.example.ankisupporter.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 単語帳・検索履歴データアクセスオブジェクト (DAO)
 */
@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>): List<Long>

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    suspend fun getAllWordsList(): List<WordEntity>

    // マイ単語帳（ユーザーが保存した単語一覧）
    @Query("SELECT * FROM words WHERE isSaved = 1 ORDER BY savedAt DESC, createdAt DESC")
    fun getSavedWords(): Flow<List<WordEntity>>

    // 検索履歴（最新100件）
    @Query("SELECT * FROM words ORDER BY searchedAt DESC LIMIT :limit")
    fun getSearchHistory(limit: Int = 100): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun findWordByText(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE isSaved = 1 AND isMemorized = 0 ORDER BY savedAt DESC, createdAt DESC")
    fun getUnmemorizedSavedWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isSaved = 1 AND isMemorized = 1 ORDER BY savedAt DESC, createdAt DESC")
    fun getMemorizedSavedWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isSaved = 1 AND (word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' OR exampleSentence LIKE '%' || :query || '%') ORDER BY savedAt DESC")
    fun searchSavedWords(query: String): Flow<List<WordEntity>>

    @Query("UPDATE words SET isMemorized = :isMemorized WHERE id = :id")
    suspend fun updateMemorizedStatus(id: Long, isMemorized: Boolean)

    @Query("UPDATE words SET isSaved = 1, savedAt = :savedAt WHERE id = :id")
    suspend fun markAsSaved(id: Long, savedAt: Long = System.currentTimeMillis())

    @Query("UPDATE words SET isSaved = 0 WHERE id = :id")
    suspend fun unmarkAsSaved(id: Long)

    @Query("UPDATE words SET reviewCount = reviewCount + 1, lastReviewedAt = :timestamp WHERE id = :id")
    suspend fun recordReview(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM words WHERE isSaved = 1")
    fun getSavedWordCount(): Flow<Int>

    // ユーザーが保存していない履歴データ (isSaved = 0) を数える
    @Query("SELECT COUNT(*) FROM words WHERE isSaved = 0")
    suspend fun getUnsavedHistoryCount(): Int

    // 100件を超えた古い未保存履歴を削除（isSaved = 1のデータは保護）
    @Query("""
        DELETE FROM words 
        WHERE isSaved = 0 
          AND id IN (
              SELECT id FROM words 
              WHERE isSaved = 0 
              ORDER BY searchedAt ASC 
              LIMIT (CASE WHEN (SELECT COUNT(*) FROM words WHERE isSaved = 0) > :limit 
                          THEN (SELECT COUNT(*) FROM words WHERE isSaved = 0) - :limit 
                          ELSE 0 END)
          )
    """)
    suspend fun trimUnsavedHistory(limit: Int = 100)

    @Transaction
    suspend fun upsertHistoryAndTrim(word: WordEntity, historyLimit: Int = 100): Long {
        val existing = findWordByText(word.word.trim())
        val idToReturn = if (existing != null) {
            val updated = existing.copy(
                phonetic = word.phonetic.ifBlank { existing.phonetic },
                translation = word.translation.ifBlank { existing.translation },
                exampleSentence = word.exampleSentence.ifBlank { existing.exampleSentence },
                exampleTranslation = word.exampleTranslation.ifBlank { existing.exampleTranslation },
                explanation = word.explanation.ifBlank { existing.explanation },
                synonyms = if (word.synonyms.isNotEmpty()) word.synonyms else existing.synonyms,
                searchedAt = System.currentTimeMillis()
            )
            updateWord(updated)
            existing.id
        } else {
            insertWord(word.copy(searchedAt = System.currentTimeMillis()))
        }
        trimUnsavedHistory(historyLimit)
        return idToReturn
    }
}
