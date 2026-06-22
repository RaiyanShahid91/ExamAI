package com.ai.studyassistant.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val SUBJECT = "subject/{exam}"
    const val TOPIC = "topic/{exam}/{subject}"
    const val QUIZ = "quiz/{exam}/{subject}/{topic}"
    const val RESULT = "result/{exam}/{subject}/{topic}"
    const val NOTES = "notes"

    fun subject(exam: String) = "subject/${Uri.encode(exam)}"
    fun topic(exam: String, subject: String) = "topic/${Uri.encode(exam)}/${Uri.encode(subject)}"
    fun quiz(exam: String, subject: String, topic: String) =
        "quiz/${Uri.encode(exam)}/${Uri.encode(subject)}/${Uri.encode(topic)}"
    fun result(exam: String, subject: String, topic: String) =
        "result/${Uri.encode(exam)}/${Uri.encode(subject)}/${Uri.encode(topic)}"
}
