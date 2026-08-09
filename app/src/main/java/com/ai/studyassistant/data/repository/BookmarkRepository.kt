package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.BookmarkedQuestionEntity
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarkRepository(context: Context) {

    private val dao = QuizDatabase.getInstance(context).bookmarkDao()

    fun observeBookmarks(): Flow<List<BookmarkedQuestionEntity>> = dao.observeAll()

    fun observeBookmarkedQuestions(): Flow<List<Question>> =
        dao.observeAll().map { list ->
            list.map { Question(it.question, it.options, it.correct, it.explanation) }
        }

    suspend fun isBookmarked(question: String): Boolean = dao.findByQuestion(question) != null

    suspend fun toggleBookmark(exam: String, subject: String, topic: String, question: Question) {
        val existing = dao.findByQuestion(question.question)
        if (existing != null) {
            dao.delete(existing)
        } else {
            dao.insert(
                BookmarkedQuestionEntity(
                    exam = exam,
                    subject = subject,
                    topic = topic,
                    question = question.question,
                    options = question.options,
                    correct = question.correct,
                    explanation = question.explanation,
                    bookmarkedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
