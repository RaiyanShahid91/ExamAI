package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.Flashcard
import com.ai.studyassistant.data.model.FlashcardDeck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Flashcards come only from the student's own missed and bookmarked questions. */
class FlashcardRepository(context: Context) {

    private val database = QuizDatabase.getInstance(context)

    fun observeCards(): Flow<List<Flashcard>> =
        combine(database.bookmarkDao().observeAll(), database.reviewDao().observeAll()) { bookmarks, reviews ->
            FlashcardDeck.merge(bookmarks, reviews)
        }
}
