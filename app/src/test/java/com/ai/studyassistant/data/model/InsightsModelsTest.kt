package com.ai.studyassistant.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsModelsTest {

    @Test fun epochDay_respectsTimeZoneOffset() {
        // 23:30 UTC on day 0 is already day 1 in UTC+1.
        val millis = 23 * 3_600_000L + 30 * 60_000L
        assertEquals(0L, StudyDays.epochDay(millis, 0))
        assertEquals(1L, StudyDays.epochDay(millis, 3_600_000))
    }

    @Test fun epochDay_handlesNegativeOffsetsBeforeEpoch() {
        assertEquals(-1L, StudyDays.epochDay(0L, -3_600_000))
    }

    @Test fun currentStreak_countsBackFromToday() {
        assertEquals(3, StudyDays.currentStreak(setOf(8L, 9L, 10L), today = 10L))
    }

    @Test fun currentStreak_stillAliveIfLastPracticeWasYesterday() {
        assertEquals(2, StudyDays.currentStreak(setOf(8L, 9L), today = 10L))
    }

    @Test fun currentStreak_brokenAfterAMissedDay() {
        assertEquals(0, StudyDays.currentStreak(setOf(7L, 8L), today = 10L))
    }

    @Test fun currentStreak_emptyIsZero() {
        assertEquals(0, StudyDays.currentStreak(emptySet(), today = 10L))
    }

    @Test fun bestStreak_findsLongestRun() {
        assertEquals(4, StudyDays.bestStreak(setOf(1L, 2L, 5L, 6L, 7L, 8L, 20L)))
        assertEquals(0, StudyDays.bestStreak(emptySet()))
        assertEquals(1, StudyDays.bestStreak(setOf(42L)))
    }

    @Test fun badges_unlockAtThresholds() {
        val none = Badges.evaluate(0, 0, 0f, 0, 0)
        assertTrue(none.none { it.earned })

        val some = Badges.evaluate(10, 100, 0.5f, 7, 1).filter { it.earned }.map { it.id }.toSet()
        assertTrue("first_quiz" in some)
        assertTrue("quizzes_10" in some)
        assertTrue("questions_100" in some)
        assertTrue("streak_7" in some)
        assertTrue("mock_1" in some)
        assertFalse("sharpshooter" in some)
        assertFalse("quizzes_50" in some)
    }

    @Test fun sharpshooter_needsVolumeAndAccuracy() {
        assertFalse(Badges.evaluate(5, 49, 0.95f, 0, 0).first { it.id == "sharpshooter" }.earned)
        assertTrue(Badges.evaluate(5, 50, 0.8f, 0, 0).first { it.id == "sharpshooter" }.earned)
        assertFalse(Badges.evaluate(5, 80, 0.79f, 0, 0).first { it.id == "sharpshooter" }.earned)
    }

    @Test fun weakest_onlyFlagsLowAccuracyTopicsWithEnoughQuestions() {
        val stats = listOf(
            TopicStat("JEE", "Physics", "Optics", 3, 15),     // 20%, flagged
            TopicStat("JEE", "Maths", "Calculus", 5, 5),      // 100%, strong
            TopicStat("JEE", "Maths", "Algebra", 0, 3),       // too few questions
            TopicStat("JEE", "Chem", "Bonding", 6, 10)        // 60%, flagged
        )
        val weak = TopicRanking.weakest(stats).map { it.topic }
        assertEquals(listOf("Optics", "Bonding"), weak)
    }

    @Test fun weakest_boundaryAtEightyPercentIsNotWeak() {
        assertTrue(TopicRanking.weakest(listOf(TopicStat("JEE", "P", "T", 8, 10))).isEmpty())
    }

    @Test fun markingScheme_appliesNegativeMarking() {
        val jee = MarkingScheme.forExam("JEE")
        assertEquals(17f, jee.marks(correct = 5, wrong = 3), 0.001f)
        assertEquals(40f, jee.maxMarks(10), 0.001f)
    }

    @Test fun markingScheme_canGoNegative() {
        assertEquals(-2f, MarkingScheme.forExam("NEET").marks(correct = 0, wrong = 2), 0.001f)
    }

    @Test fun markingScheme_unknownExamHasNoPenalty() {
        assertEquals(3f, MarkingScheme.forExam("Other").marks(correct = 3, wrong = 5), 0.001f)
    }
}
