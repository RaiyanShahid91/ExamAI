package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.local.ExamAttemptEntity
import com.ai.studyassistant.data.model.Badges
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.InsightsData
import com.ai.studyassistant.data.model.StudyDays
import com.ai.studyassistant.data.model.TopicRanking
import com.ai.studyassistant.data.model.SubjectStat
import com.ai.studyassistant.data.model.TopicStat
import com.ai.studyassistant.data.model.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
private const val MIN_ATTEMPTS_FOR_WEAK_TOPIC = 1

class AnalyticsRepository(context: Context) {

    private val dao = QuizDatabase.getInstance(context).examAttemptDao()

    private fun completedAttempts(): Flow<List<ExamAttemptSummary>> =
        dao.observeAll().map { list -> list.map { it.toSummary() }.filter { it.isCompleted } }

    fun subjectStats(): Flow<List<SubjectStat>> =
        completedAttempts().map { attempts ->
            attempts.groupBy { it.subject }
                .map { (subject, group) ->
                    SubjectStat(
                        subject = subject,
                        correct = group.sumOf { it.score },
                        total = group.sumOf { it.total }
                    )
                }
                .sortedBy { it.accuracy }
        }

    fun weakestTopic(): Flow<TopicStat?> =
        completedAttempts().map { attempts ->
            attempts.groupBy { Triple(it.exam, it.subject, it.topic) }
                .map { (key, group) ->
                    TopicStat(
                        exam = key.first,
                        subject = key.second,
                        topic = key.third,
                        correct = group.sumOf { it.score },
                        total = group.sumOf { it.total }
                    )
                }
                .filter { it.total >= MIN_ATTEMPTS_FOR_WEAK_TOPIC }
                .minByOrNull { it.accuracy }
        }

    fun currentStreak(): Flow<Int> =
        dao.observeAll().map { list ->
            StudyDays.currentStreak(studyDays(list), StudyDays.epochDay(System.currentTimeMillis()))
        }

    /** One-shot snapshot used by the Insights screen and the home-screen widget. */
    suspend fun insightsSnapshot(): InsightsData = buildInsights(dao.getAll())

    fun insights(): Flow<InsightsData> = dao.observeAll().map { buildInsights(it) }

    private fun studyDays(attempts: List<ExamAttemptEntity>): Set<Long> =
        attempts.map { StudyDays.epochDay(it.startTime) }.toSet()

    private fun buildInsights(entities: List<ExamAttemptEntity>): InsightsData {
        val days = studyDays(entities)
        val completed = entities.filter { it.isCompleted }.map { it.toSummary() }
        val questionsAnswered = completed.sumOf { it.answeredCount }
        val correct = completed.sumOf { it.score }
        val accuracy = if (questionsAnswered == 0) 0f else correct.toFloat() / questionsAnswered
        val best = StudyDays.bestStreak(days)

        val weakTopics = TopicRanking.weakest(
            completed
                .filter { it.subject != MOCK_SUBJECT }
                .groupBy { Triple(it.exam, it.subject, it.topic) }
                .map { (key, group) ->
                    TopicStat(key.first, key.second, key.third, group.sumOf { it.score }, group.sumOf { it.answeredCount })
                }
        )

        val subjectStats = completed
            .filter { it.subject != MOCK_SUBJECT }
            .groupBy { it.subject }
            .map { (subject, group) -> SubjectStat(subject, group.sumOf { it.score }, group.sumOf { it.answeredCount }) }
            .sortedBy { it.accuracy }

        return InsightsData(
            quizzesCompleted = completed.size,
            questionsAnswered = questionsAnswered,
            correctAnswers = correct,
            currentStreak = StudyDays.currentStreak(days, StudyDays.epochDay(System.currentTimeMillis())),
            bestStreak = best,
            studyDays = days,
            weakTopics = weakTopics,
            subjectStats = subjectStats,
            badges = Badges.evaluate(
                quizzesCompleted = completed.size,
                questionsAnswered = questionsAnswered,
                accuracy = accuracy,
                bestStreak = best,
                mockExamsCompleted = entities.count { it.isCompleted && it.isMockExam }
            )
        )
    }

    private companion object {
        const val MOCK_SUBJECT = "All Subjects"
    }
}
