package com.ai.studyassistant.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.toSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds a CSV of the user's own quiz history in the app cache so they can share/save it. */
object HistoryExporter {

    private const val HEADER = "date,exam,subject,topic,type,questions,answered,correct,accuracy_percent,minutes"

    /** Returns a shareable URI, or null when there is no completed attempt to export. */
    suspend fun exportCsv(context: Context): Uri? = withContext(Dispatchers.IO) {
        val attempts = QuizDatabase.getInstance(context).examAttemptDao().getAll().filter { it.isCompleted }
        if (attempts.isEmpty()) return@withContext null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val csv = buildString {
            appendLine(HEADER)
            attempts.forEach { entity ->
                val s = entity.toSummary()
                val type = when {
                    entity.isMockExam -> "mock"
                    entity.isTimed -> "timed"
                    else -> "practice"
                }
                val minutes = s.durationMillis?.let { it / 60_000 }?.toString().orEmpty()
                val accuracy = if (s.answeredCount == 0) 0 else s.score * 100 / s.answeredCount
                appendLine(
                    listOf(
                        dateFormat.format(Date(s.startTime)), s.exam, s.subject, s.topic, type,
                        s.total.toString(), s.answeredCount.toString(), s.score.toString(),
                        accuracy.toString(), minutes
                    ).joinToString(",") { csvField(it) }
                )
            }
        }

        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "examai_history.csv")
        file.writeText(csv)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Quotes a field and neutralises spreadsheet formula prefixes. */
    internal fun csvField(value: String): String {
        val safe = if (value.firstOrNull() in setOf('=', '+', '-', '@')) "'$value" else value
        return "\"" + safe.replace("\"", "\"\"") + "\""
    }
}
