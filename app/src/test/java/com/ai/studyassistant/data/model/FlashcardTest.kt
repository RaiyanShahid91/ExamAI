package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.BookmarkedQuestionEntity
import com.ai.studyassistant.data.local.ReviewItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashcardTest {

    private fun review(q: String, correct: Int = 0, options: List<String> = listOf("A", "B")) =
        ReviewItemEntity(exam = "JEE", subject = "Physics", topic = "Optics", question = q, options = options,
            correct = correct, explanation = "why", dueAt = 0, createdAt = 0)

    private fun bookmark(q: String, correct: Int = 0, options: List<String> = listOf("A", "B")) =
        BookmarkedQuestionEntity(exam = "JEE", subject = "Physics", topic = "Optics", question = q, options = options,
            correct = correct, explanation = "why", bookmarkedAt = 0)

    private fun card(q: String) = Flashcard(q, listOf("A", "B"), 0, "e", "JEE", "P", "T", FlashcardSource.MISTAKE)

    // ---- deck ----

    @Test fun merge_combinesMistakesAndBookmarks() {
        val deck = FlashcardDeck.merge(listOf(bookmark("saved")), listOf(review("missed")))
        assertEquals(setOf("saved", "missed"), deck.map { it.question }.toSet())
        assertEquals(FlashcardSource.MISTAKE, deck.first { it.question == "missed" }.source)
        assertEquals(FlashcardSource.SAVED, deck.first { it.question == "saved" }.source)
    }

    @Test fun merge_dedupesQuestionInBothAndMarksBoth() {
        val deck = FlashcardDeck.merge(listOf(bookmark("same")), listOf(review("same")))
        assertEquals(1, deck.size)
        assertEquals(FlashcardSource.BOTH, deck.single().source)
    }

    @Test fun merge_skipsEntriesWithInvalidAnswerIndex() {
        val deck = FlashcardDeck.merge(
            listOf(bookmark("bad", correct = 5)),
            listOf(review("bad2", correct = -1), review("ok", correct = 1))
        )
        assertEquals(listOf("ok"), deck.map { it.question })
    }

    @Test fun merge_emptyInputsGiveEmptyDeck() {
        assertTrue(FlashcardDeck.merge(emptyList(), emptyList()).isEmpty())
    }

    @Test fun answer_isTheCorrectOptionText() {
        assertEquals("B", FlashcardDeck.merge(emptyList(), listOf(review("q", correct = 1))).single().answer)
    }

    // ---- session ----

    private val identity: (List<Flashcard>) -> List<Flashcard> = { it }

    @Test fun session_startsOnFirstCardUnflipped() {
        val s = FlashcardSession.start(listOf(card("1"), card("2")), identity)
        assertEquals("1", s.current?.question)
        assertEquals(2, s.total)
        assertEquals(0, s.known)
        assertFalse(s.flipped)
        assertFalse(s.isComplete)
    }

    @Test fun session_flipTogglesAndGotItAdvancesAndResetsFlip() {
        val s = FlashcardSession.start(listOf(card("1"), card("2")), identity).flip()
        assertTrue(s.flipped)
        val next = s.gotIt()
        assertEquals("2", next.current?.question)
        assertFalse(next.flipped)
        assertEquals(1, next.known)
    }

    @Test fun session_againSendsCardToBackAndCountsRetry() {
        val s = FlashcardSession.start(listOf(card("1"), card("2")), identity).flip().again()
        assertEquals(listOf("2", "1"), s.queue.map { it.question })
        assertEquals(1, s.retries)
        assertEquals(0, s.known)
        assertFalse(s.flipped)
    }

    @Test fun session_completesOnlyWhenEveryCardIsGotIt() {
        var s = FlashcardSession.start(listOf(card("1"), card("2")), identity)
        s = s.again()       // 1 goes to the back: [2, 1]
        s = s.gotIt()       // 2 done: [1]
        assertFalse(s.isComplete)
        s = s.gotIt()       // 1 done
        assertTrue(s.isComplete)
        assertEquals(2, s.known)
        assertEquals(1, s.retries)
        assertNull(s.current)
    }

    @Test fun session_emptyDeckIsEmptyNotComplete() {
        val s = FlashcardSession.start(emptyList(), identity)
        assertTrue(s.isEmpty)
        assertFalse(s.isComplete)
        assertEquals(s, s.flip().gotIt().again())   // no-ops on an empty deck
    }

    @Test fun session_singleCardAgainStaysOnThatCard() {
        val s = FlashcardSession.start(listOf(card("only")), identity).again()
        assertEquals("only", s.current?.question)
        assertEquals(1, s.retries)
    }
}
