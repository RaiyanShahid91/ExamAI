package com.ai.studyassistant.data.repository

import android.content.Context
import android.text.Html
import com.ai.studyassistant.data.api.OpenTriviaService
import com.ai.studyassistant.data.api.TriviaQuestionDto
import com.ai.studyassistant.data.local.CachedQuestionEntity
import com.ai.studyassistant.data.local.LocalQuestionBank
import com.ai.studyassistant.data.local.NetworkUtils
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.model.Question

private const val QUIZ_SIZE = 10
private const val MAX_REMOTE_BLEND = 4

class QuestionRepository(
    private val context: Context,
    private val triviaService: OpenTriviaService = OpenTriviaService.create()
) {
    private val questionDao = QuizDatabase.getInstance(context).questionDao()

    suspend fun getQuiz(exam: String, subject: String, topic: String): Result<List<Question>> {
        val bankKey = ExamData.localBankKey(exam, subject)
            ?: return Result.failure(IllegalArgumentException("No question bank for $exam/$subject"))

        val localQuestions = LocalQuestionBank.questionsFor(context, bankKey).shuffled()
        val remoteQuestions = fetchRemoteBlend(bankKey, subject)

        val remoteCount = remoteQuestions.size.coerceAtMost(MAX_REMOTE_BLEND)
        val blended = (remoteQuestions.take(remoteCount) + localQuestions)
            .distinctBy { it.question }
            .shuffled()
            .take(QUIZ_SIZE)

        return if (blended.isEmpty()) {
            Result.failure(IllegalStateException("No questions available for $topic right now."))
        } else {
            Result.success(blended)
        }
    }

    suspend fun fetchDailyQuestion(): Question? {
        if (!NetworkUtils.isOnline(context)) return null
        return try {
            val response = triviaService.getQuestions(amount = 1, category = 17)
            response.results.firstOrNull()?.toQuestion()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchRemoteBlend(bankKey: String, subject: String): List<Question> {
        val category = ExamData.triviaCategoryFor(subject)
        val online = NetworkUtils.isOnline(context)

        if (online && category != null) {
            try {
                val response = triviaService.getQuestions(amount = QUIZ_SIZE, category = category)
                val mapped = response.results.map { it.toQuestion() }
                if (mapped.isNotEmpty()) {
                    questionDao.replaceBankKey(bankKey, mapped.map { it.toEntity(bankKey) })
                    return mapped
                }
            } catch (_: Exception) {
                // fall through to cached/local
            }
        }

        return questionDao.getByBankKey(bankKey).map { it.toQuestion() }
    }

    private fun TriviaQuestionDto.toQuestion(): Question {
        val decodedQuestion = decodeHtml(question)
        val correctText = decodeHtml(correct_answer)
        val options = (incorrect_answers.map { decodeHtml(it) } + correctText).shuffled()
        val correctIndex = options.indexOf(correctText).coerceAtLeast(0)
        return Question(
            question = decodedQuestion,
            options = options,
            correct = correctIndex,
            explanation = "Correct answer: $correctText"
        )
    }

    private fun decodeHtml(text: String): String =
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()

    private fun Question.toEntity(bankKey: String) = CachedQuestionEntity(
        bankKey = bankKey,
        question = question,
        options = options,
        correct = correct,
        explanation = explanation
    )

    private fun CachedQuestionEntity.toQuestion() = Question(
        question = question,
        options = options,
        correct = correct,
        explanation = explanation
    )
}
