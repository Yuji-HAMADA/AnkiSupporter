package com.example.ankisupporter.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 単語帳データアクセスオブジェクト (DAO)
 */
@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isMemorized = 0 ORDER BY createdAt DESC")
    fun getUnmemorizedWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isMemorized = 1 ORDER BY createdAt DESC")
    fun getMemorizedWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("UPDATE words SET isMemorized = :isMemorized WHERE id = :id")
    suspend fun updateMemorizedStatus(id: Long, isMemorized: Boolean)

    @Query("UPDATE words SET reviewCount = reviewCount + 1, lastReviewedAt = :timestamp WHERE id = :id")
    suspend fun recordReview(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM words")
    fun getWordCount(): Flow<Int>
}

