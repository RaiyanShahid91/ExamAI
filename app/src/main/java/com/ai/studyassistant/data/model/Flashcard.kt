package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.BookmarkedQuestionEntity
import com.ai.studyassistant.data.local.ReviewItemEntity

enum class FlashcardSource { MISTAKE, SAVED, BOTH }

/** A question the student missed or bookmarked, shown question-first with the answer on the back. */
data class Flashcard(
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val source: FlashcardSource
) {
    val answer: String get() = options.getOrElse(correct) { "" }
}

object FlashcardDeck {

    /**
     * Builds the deck from questions the student got wrong and ones they bookmarked, with no
     * duplicates (a question in both lists is marked [FlashcardSource.BOTH]). Entries whose
     * stored answer index doesn't point at an option are skipped rather than shown wrong.
     */
    fun merge(bookmarks: List<BookmarkedQuestionEntity>, reviews: List<ReviewItemEntity>): List<Flashcard> {
        val savedQuestions = bookmarks.map { it.question }.toSet()
        val mistakeQuestions = reviews.map { it.question }.toSet()

        val fromReviews = reviews.map {
            Flashcard(
                it.question, it.options, it.correct, it.explanation, it.exam, it.subject, it.topic,
                if (it.question in savedQuestions) FlashcardSource.BOTH else FlashcardSource.MISTAKE
            )
        }
        val fromBookmarks = bookmarks.filter { it.question !in mistakeQuestions }.map {
            Flashcard(it.question, it.options, it.correct, it.explanation, it.exam, it.subject, it.topic, FlashcardSource.SAVED)
        }
        return (fromReviews + fromBookmarks)
            .filter { it.correct in it.options.indices }
            .distinctBy { it.question }
    }
}

/**
 * One pass through a deck. "Got it" retires a card; "Again" sends it to the back of the queue
 * so it comes round once more. Immutable so each step is easy to reason about and test.
 */
data class FlashcardSession(
    val queue: List<Flashcard>,
    val total: Int,
    val flipped: Boolean = false,
    val retries: Int = 0
) {
    val current: Flashcard? get() = queue.firstOrNull()
    val known: Int get() = total - queue.size
    val isEmpty: Boolean get() = total == 0
    val isComplete: Boolean get() = total > 0 && queue.isEmpty()

    fun flip(): FlashcardSession = if (current == null) this else copy(flipped = !flipped)

    fun gotIt(): FlashcardSession = if (current == null) this else copy(queue = queue.drop(1), flipped = false)

    fun again(): FlashcardSession {
        val card = current ?: return this
        return copy(queue = queue.drop(1) + card, flipped = false, retries = retries + 1)
    }

    companion object {
        fun start(cards: List<Flashcard>, order: (List<Flashcard>) -> List<Flashcard> = { it.shuffled() }) =
            FlashcardSession(queue = order(cards), total = cards.size)
    }
}
