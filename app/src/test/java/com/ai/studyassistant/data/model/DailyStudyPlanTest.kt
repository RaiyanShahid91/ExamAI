package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.ExamAttemptEntity
import com.ai.studyassistant.data.local.ReviewItemEntity
import com.ai.studyassistant.data.local.StudyPlanSettings
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DailyStudyPlanTest {
    private val now = Instant.parse("2026-10-31T12:00:00Z").toEpochMilli()
    private val zone = ZoneId.of("Asia/Kolkata")
    private val question = Question("Test", listOf("A", "B"), 0, "Explanation")
    private fun attempt(exam: String = "JEE", end: Long = now, completed: Boolean = true, topic: String = "Mechanics", answer: Int = 0) =
        ExamAttemptEntity(exam = exam, subject = "Physics", topic = topic, startTime = now - 86_400_000,
            endTime = end, questions = listOf(question), answers = listOf(answer), isCompleted = completed)

    @Test fun countsOnlyAnsweredQuestionsFromCompletedSelectedExamToday() {
        val plan = buildDailyStudyPlan(StudyPlanSettings(), listOf(
            attempt(), attempt(completed = false), attempt(exam = "NEET"),
            attempt(end = now - 86_400_000), attempt(answer = -1)
        ), emptyList(), now, zone)
        assertEquals(1, plan.answered)
        assertTrue(plan.topicCompleted)
    }

    @Test fun recommendationStaysFixedAfterTodaysPractice() {
        val earlier = attempt(end = now - 86_400_000, topic = "Optics", answer = 1)
        val plan = buildDailyStudyPlan(StudyPlanSettings(), listOf(earlier, attempt(topic = "Optics")), emptyList(), now, zone)
        assertEquals("Optics", plan.topic)
        assertTrue(plan.topicCompleted)
    }

    @Test fun progressResetsAtLocalMidnight() {
        val plan = buildDailyStudyPlan(StudyPlanSettings(), listOf(attempt()), emptyList(),
            Instant.parse("2026-10-31T18:30:00Z").toEpochMilli(), zone)
        assertEquals(0, plan.answered)
        assertFalse(plan.topicCompleted)
    }

    @Test fun reviewsAreScopedToExamAndNewMistakesDoNotCountAsReviews() {
        val review = ReviewItemEntity(exam = "JEE", subject = "Physics", topic = "Mechanics",
            question = "Test", options = listOf("A", "B"), correct = 0, explanation = "",
            dueAt = now - 1, createdAt = now - 86_400_000)
        val plan = buildDailyStudyPlan(StudyPlanSettings(), emptyList(), listOf(
            review, review.copy(exam = "NEET"),
            review.copy(dueAt = now + 86_400_000, lastReviewedAt = now),
            review.copy(createdAt = now, dueAt = now + 86_400_000)
        ), now, zone)
        assertEquals(1, plan.dueReviews)
        assertEquals(1, plan.reviewedToday)
    }

    @Test fun newStudentGetsValidTopic() {
        val plan = buildDailyStudyPlan(StudyPlanSettings("CAT", 20), emptyList(), emptyList(), now, zone)
        assertTrue(plan.topic in ExamData.topicsFor("CAT", plan.subject))
        assertEquals(20, plan.settings.questionGoal)
    }
}
