package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.ExamAttemptEntity
import com.ai.studyassistant.data.local.ReviewItemEntity
import com.ai.studyassistant.data.local.StudyPlanSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyStudyPlan(
    val settings: StudyPlanSettings,
    val answered: Int,
    val subject: String,
    val topic: String,
    val topicCompleted: Boolean,
    val dueReviews: Int,
    val reviewedToday: Int,
    /** Days until the exam: >0 countdown, 0 exam day, <0 date passed, null if no date set. */
    val daysLeft: Int? = null,
    /** Goal that would reach the practice target by the exam date, if one is set and in the future. */
    val suggestedGoal: Int? = null
)

/** Uses completion dates in the student's timezone; unfinished quizzes never count. */
fun buildDailyStudyPlan(
    settings: StudyPlanSettings,
    attempts: List<ExamAttemptEntity>,
    reviews: List<ReviewItemEntity>,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault()
): DailyStudyPlan {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    fun date(time: Long): LocalDate = Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
    val completed = attempts.filter { it.exam == settings.exam && it.isCompleted && it.endTime != null }
    val todayAttempts = completed.filter { date(it.endTime!!) == today }
    // Only earlier days influence the recommendation so today's completion cannot move the goalpost.
    val priorTopics = completed.filter { date(it.endTime!!) < today && !it.isMockExam }
        .filter { it.topic in ExamData.topicsFor(settings.exam, it.subject) }
        .groupBy { it.subject to it.topic }
    val weakest = priorTopics.entries.filter { (_, group) -> group.any { a -> a.answers.any { it >= 0 } } }
        .minByOrNull { (_, group) ->
            val answered = group.sumOf { a -> a.answers.count { it >= 0 } }
            val correct = group.sumOf { a -> a.answers.indices.count { i -> a.answers[i] >= 0 && a.answers[i] == a.questions.getOrNull(i)?.correct } }
            correct.toDouble() / answered.coerceAtLeast(1)
        }?.key
    val subject = weakest?.first ?: ExamData.subjectsFor(settings.exam).first().name
    val topic = weakest?.second ?: ExamData.topicsFor(settings.exam, subject).first()
    val examReviews = reviews.filter { it.exam == settings.exam }
    val daysLeft = settings.examDateEpochDay?.let { ExamPacing.daysLeft(it, today.toEpochDay()) }
    val answeredForExam = completed.sumOf { a -> a.answers.take(a.questions.size).count { it >= 0 } }
    return DailyStudyPlan(
        settings = settings,
        answered = todayAttempts.sumOf { a -> a.answers.take(a.questions.size).count { it >= 0 } },
        subject = subject,
        topic = topic,
        topicCompleted = todayAttempts.any { it.subject == subject && it.topic == topic && it.answers.any { answer -> answer >= 0 } },
        dueReviews = examReviews.count { it.dueAt <= now },
        daysLeft = daysLeft,
        suggestedGoal = daysLeft?.let { ExamPacing.suggestedGoal(it, answeredForExam) },
        reviewedToday = examReviews.count { it.lastReviewedAt?.let { time -> date(time) == today } == true && it.createdAt < today.atStartOfDay(zone).toInstant().toEpochMilli() }
    )
}
