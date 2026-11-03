package com.ai.studyassistant.data.model

/** Turns an exam date into a countdown and a suggested daily question goal. */
object ExamPacing {

    /** Practice questions we suggest answering per exam before the big day. */
    const val PRACTICE_TARGET = 1000
    const val MIN_GOAL = 5
    const val MAX_GOAL = 50
    private const val STEP = 5

    /** Whole days from [todayEpochDay] to the exam. Zero is exam day; negative means it has passed. */
    fun daysLeft(examEpochDay: Long, todayEpochDay: Long): Int = (examEpochDay - todayEpochDay).toInt()

    /**
     * Daily goal that spreads the remaining practice target over the days left, rounded up to
     * a multiple of 5 and kept within the app's 5–50 goal range. Null once there are no
     * days left to plan for.
     */
    fun suggestedGoal(daysLeft: Int, answeredSoFar: Int): Int? {
        if (daysLeft <= 0) return null
        val remaining = (PRACTICE_TARGET - answeredSoFar).coerceAtLeast(0)
        val perDay = (remaining + daysLeft - 1) / daysLeft
        val rounded = (perDay + STEP - 1) / STEP * STEP
        return rounded.coerceIn(MIN_GOAL, MAX_GOAL)
    }
}
