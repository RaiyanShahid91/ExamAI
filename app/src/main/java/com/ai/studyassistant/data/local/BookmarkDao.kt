package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarked_questions ORDER BY bookmarkedAt DESC")
    fun observeAll(): Flow<List<BookmarkedQuestionEntity>>

    @Query("SELECT * FROM bookmarked_questions WHERE question = :question LIMIT 1")
    suspend fun findByQuestion(question: String): BookmarkedQuestionEntity?

    @Insert
    suspend fun insert(bookmark: BookmarkedQuestionEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkedQuestionEntity)
}
