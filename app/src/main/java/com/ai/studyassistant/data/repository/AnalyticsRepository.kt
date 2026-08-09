package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.SubjectStat
import com.ai.studyassistant.data.model.TopicStat
import com.ai.studyassistant.data.model.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

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
        dao.observeAll().map { list -> computeStreak(list.map { it.startTime }) }

    private fun computeStreak(startTimes: List<Long>): Int {
        if (startTimes.isEmpty()) return 0
        val days = startTimes.map { dayKey(it) }.toSet()
        var streak = 0
        val cal = Calendar.getInstance()
        while (days.contains(dayKey(cal.timeInMillis))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun dayKey(millis: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }
}
