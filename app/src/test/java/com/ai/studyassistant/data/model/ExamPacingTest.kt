package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.ExamAttemptEntity
import com.ai.studyassistant.data.local.StudyPlanSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ExamPacingTest {

    @Test fun daysLeft_countsWholeDays() {
        assertEquals(10, ExamPacing.daysLeft(examEpochDay = 110, todayEpochDay = 100))
        assertEquals(0, ExamPacing.daysLeft(100, 100))
        assertEquals(-3, ExamPacing.daysLeft(97, 100))
    }

    @Test fun suggestedGoal_spreadsRemainingTargetAcrossDays() {
        // 1000 questions over 100 days = 10/day.
        assertEquals(10, ExamPacing.suggestedGoal(daysLeft = 100, answeredSoFar = 0))
        // 1000 over 40 days = 25/day.
        assertEquals(25, ExamPacing.suggestedGoal(40, 0))
    }

    @Test fun suggestedGoal_roundsUpToMultipleOfFive() {
        // 1000 / 150 = 6.67 -> 7 -> 10.
        assertEquals(10, ExamPacing.suggestedGoal(150, 0))
    }

    @Test fun suggestedGoal_isCappedAtFiftyWhenTimeIsShort() {
        assertEquals(50, ExamPacing.suggestedGoal(daysLeft = 5, answeredSoFar = 0))
    }

    @Test fun suggestedGoal_dropsToMinimumOnceTargetIsReached() {
        assertEquals(5, ExamPacing.suggestedGoal(daysLeft = 30, answeredSoFar = 1000))
        assertEquals(5, ExamPacing.suggestedGoal(daysLeft = 30, answeredSoFar = 5000))
    }

    @Test fun suggestedGoal_accountsForProgressSoFar() {
        // 500 left over 25 days = 20/day.
        assertEquals(20, ExamPacing.suggestedGoal(25, 500))
    }

    @Test fun suggestedGoal_isNullWhenNoDaysLeft() {
        assertNull(ExamPacing.suggestedGoal(0, 0))
        assertNull(ExamPacing.suggestedGoal(-4, 0))
    }

    // ---- integration with the daily plan ----

    private val zone = ZoneId.of("Asia/Kolkata")
    private val now = Instant.parse("2026-10-31T12:00:00Z").toEpochMilli()
    private val today = LocalDate.of(2026, 10, 31).toEpochDay()
    private val question = Question("Q", listOf("A", "B"), 0, "E")

    private fun attempt(exam: String = "JEE", answers: List<Int>) = ExamAttemptEntity(
        exam = exam, subject = "Physics", topic = "Mechanics", startTime = now - 3_600_000, endTime = now - 1_800_000,
        questions = answers.map { question }, answers = answers, isCompleted = true
    )

    @Test fun plan_withoutExamDateHasNoCountdown() {
        val plan = buildDailyStudyPlan(StudyPlanSettings(), emptyList(), emptyList(), now, zone)
        assertNull(plan.daysLeft)
        assertNull(plan.suggestedGoal)
    }

    @Test fun plan_withFutureExamDateHasCountdownAndSuggestion() {
        val settings = StudyPlanSettings(examDateEpochDay = today + 40)
        val plan = buildDailyStudyPlan(settings, emptyList(), emptyList(), now, zone)
        assertEquals(40, plan.daysLeft)
        assertEquals(25, plan.suggestedGoal)
    }

    @Test fun plan_suggestionUsesOnlySelectedExamsAnsweredQuestions() {
        val settings = StudyPlanSettings(exam = "JEE", examDateEpochDay = today + 25)
        val attempts = listOf(
            attempt("JEE", List(500) { 0 }),
            attempt("NEET", List(500) { 0 })   // must not count towards JEE's progress
        )
        val plan = buildDailyStudyPlan(settings, attempts, emptyList(), now, zone)
        assertEquals(20, plan.suggestedGoal)
    }

    @Test fun plan_pastExamDateHasNegativeDaysAndNoSuggestion() {
        val plan = buildDailyStudyPlan(StudyPlanSettings(examDateEpochDay = today - 2), emptyList(), emptyList(), now, zone)
        assertEquals(-2, plan.daysLeft)
        assertNull(plan.suggestedGoal)
    }

    @Test fun plan_examDayHasZeroDaysAndNoSuggestion() {
        val plan = buildDailyStudyPlan(StudyPlanSettings(examDateEpochDay = today), emptyList(), emptyList(), now, zone)
        assertEquals(0, plan.daysLeft)
        assertNull(plan.suggestedGoal)
    }
}
