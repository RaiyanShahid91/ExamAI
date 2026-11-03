package com.ai.studyassistant.data.model

import java.util.TimeZone
import androidx.annotation.StringRes
import com.ai.studyassistant.R

/** An achievement shown on the Insights screen. */
data class Badge(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val earned: Boolean
)

/** Everything the Insights screen shows, derived purely from on-device quiz history. */
data class InsightsData(
    val quizzesCompleted: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    /** Days (as epoch-day numbers in the device's time zone) on which the user practised. */
    val studyDays: Set<Long> = emptySet(),
    val weakTopics: List<TopicStat> = emptyList(),
    val subjectStats: List<SubjectStat> = emptyList(),
    val badges: List<Badge> = emptyList()
) {
    val accuracy: Float
        get() = if (questionsAnswered == 0) 0f else correctAnswers.toFloat() / questionsAnswered
}

/** Pure date/streak helpers, kept free of Android types so they are unit-testable. */
object StudyDays {

    private const val DAY_MILLIS = 86_400_000L

    /** Epoch-day number of [millis] in the time zone whose UTC offset is [offsetMillis]. */
    fun epochDay(millis: Long, offsetMillis: Int = TimeZone.getDefault().getOffset(millis)): Long =
        Math.floorDiv(millis + offsetMillis, DAY_MILLIS)

    /**
     * Consecutive practice days ending today. A streak that ended yesterday still counts
     * as alive (you have until the end of today to continue it), so opening the app in the
     * morning doesn't show 0.
     */
    fun currentStreak(days: Set<Long>, today: Long): Int {
        if (days.isEmpty()) return 0
        var cursor = if (today in days) today else today - 1
        var streak = 0
        while (cursor in days) {
            streak++
            cursor--
        }
        return streak
    }

    /** Longest run of consecutive practice days in [days]. */
    fun bestStreak(days: Set<Long>): Int {
        if (days.isEmpty()) return 0
        var best = 0
        var run = 0
        var previous: Long? = null
        for (day in days.sorted()) {
            run = if (previous != null && day == previous + 1) run + 1 else 1
            if (run > best) best = run
            previous = day
        }
        return best
    }
}

/** Picks the topics worth flagging as "needs more practice". */
object TopicRanking {
    const val MIN_QUESTIONS = 5
    const val WEAK_BELOW = 0.8f
    const val MAX_TOPICS = 5

    fun weakest(stats: List<TopicStat>): List<TopicStat> =
        stats.filter { it.total >= MIN_QUESTIONS && it.accuracy < WEAK_BELOW }
            .sortedBy { it.accuracy }
            .take(MAX_TOPICS)
}

/** Achievement rules. Pure so thresholds are easy to test and tweak. */
object Badges {

    fun evaluate(
        quizzesCompleted: Int,
        questionsAnswered: Int,
        accuracy: Float,
        bestStreak: Int,
        mockExamsCompleted: Int
    ): List<Badge> = listOf(
        Badge("first_quiz", R.string.badge_first_quiz_title, R.string.badge_first_quiz_desc, quizzesCompleted >= 1),
        Badge("quizzes_10", R.string.badge_quizzes_10_title, R.string.badge_quizzes_10_desc, quizzesCompleted >= 10),
        Badge("quizzes_50", R.string.badge_quizzes_50_title, R.string.badge_quizzes_50_desc, quizzesCompleted >= 50),
        Badge("questions_100", R.string.badge_questions_100_title, R.string.badge_questions_100_desc, questionsAnswered >= 100),
        Badge("questions_500", R.string.badge_questions_500_title, R.string.badge_questions_500_desc, questionsAnswered >= 500),
        Badge("streak_3", R.string.badge_streak_3_title, R.string.badge_streak_3_desc, bestStreak >= 3),
        Badge("streak_7", R.string.badge_streak_7_title, R.string.badge_streak_7_desc, bestStreak >= 7),
        Badge("streak_30", R.string.badge_streak_30_title, R.string.badge_streak_30_desc, bestStreak >= 30),
        Badge("sharpshooter", R.string.badge_sharpshooter_title, R.string.badge_sharpshooter_desc, questionsAnswered >= 50 && accuracy >= 0.8f),
        Badge("mock_1", R.string.badge_mock_1_title, R.string.badge_mock_1_desc, mockExamsCompleted >= 1)
    )
}

/** Marks awarded per exam, mirroring each exam's real scheme (approximate; for practice only). */
data class MarkingScheme(val correctMarks: Float, val wrongPenalty: Float) {

    fun marks(correct: Int, wrong: Int): Float = correct * correctMarks - wrong * wrongPenalty

    fun maxMarks(totalQuestions: Int): Float = totalQuestions * correctMarks

    companion object {
        fun forExam(exam: String): MarkingScheme = when (exam) {
            "JEE" -> MarkingScheme(4f, 1f)
            "NEET" -> MarkingScheme(4f, 1f)
            "UPSC" -> MarkingScheme(2f, 2f / 3f)
            "SSC" -> MarkingScheme(2f, 0.5f)
            "Banking" -> MarkingScheme(1f, 0.25f)
            "CAT" -> MarkingScheme(3f, 1f)
            "CDS/NDA" -> MarkingScheme(1f, 1f / 3f)
            else -> MarkingScheme(1f, 0f)
        }
    }
}
