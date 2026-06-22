package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface QuestionDao {

    @Query("SELECT * FROM cached_questions WHERE bankKey = :bankKey")
    suspend fun getByBankKey(bankKey: String): List<CachedQuestionEntity>

    @Query("DELETE FROM cached_questions WHERE bankKey = :bankKey")
    suspend fun clearBankKey(bankKey: String)

    @Insert
    suspend fun insertAll(questions: List<CachedQuestionEntity>)

    @Transaction
    suspend fun replaceBankKey(bankKey: String, questions: List<CachedQuestionEntity>) {
        clearBankKey(bankKey)
        insertAll(questions)
    }
}
