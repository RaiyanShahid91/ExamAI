package com.ai.studyassistant.navigation

import android.net.Uri

object Routes {
    const val REMINDERS = "reminders"
    const val HOME = "home"
    const val SUBJECT = "subject/{exam}"
    const val TOPIC = "topic/{exam}/{subject}"
    const val QUIZ = "quiz/{exam}/{subject}/{topic}?difficulty={difficulty}&timed={timed}"
    const val RESULT = "result/{exam}/{subject}/{topic}?fromHistory={fromHistory}"
    const val NOTES = "notes"
    const val ABOUT = "about"
    const val PRIVACY_POLICY = "privacy_policy"
    const val HISTORY = "history"
    const val BOOKMARKS = "bookmarks"
    const val REVIEW = "review"
    const val ATTEMPT_DETAIL = "attempt/{attemptId}"
    const val QUIZ_RESUME = "quiz_resume/{attemptId}"
    const val MOCK_QUIZ = "mock_quiz/{exam}?timed={timed}"
    const val ONBOARDING = "onboarding"
    const val SETTINGS = "settings"
    const val TERMS = "terms"
    const val AI_INFO = "ai_info"
    const val INSIGHTS = "insights"
    const val FLASHCARDS = "flashcards"

    fun subject(exam: String) = "subject/${Uri.encode(exam)}"
    fun topic(exam: String, subject: String) = "topic/${Uri.encode(exam)}/${Uri.encode(subject)}"
    fun quiz(exam: String, subject: String, topic: String, difficulty: String? = null, timed: Boolean = false) =
        "quiz/${Uri.encode(exam)}/${Uri.encode(subject)}/${Uri.encode(topic)}" +
            "?difficulty=${Uri.encode(difficulty ?: "")}&timed=$timed"
    fun result(exam: String, subject: String, topic: String, fromHistory: Boolean = false) =
        "result/${Uri.encode(exam)}/${Uri.encode(subject)}/${Uri.encode(topic)}?fromHistory=$fromHistory"
    fun attemptDetail(attemptId: Long) = "attempt/$attemptId"
    fun quizResume(attemptId: Long) = "quiz_resume/$attemptId"
    fun mockQuiz(exam: String, timed: Boolean = true) = "mock_quiz/${Uri.encode(exam)}?timed=$timed"
}
