package com.ai.studyassistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.StaggeredEntrance
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.WrongRed
import com.ai.studyassistant.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AttemptDetailScreen(
    attemptId: Long,
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val detail by viewModel.detail.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(attemptId) {
        viewModel.loadDetail(attemptId)
    }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(
                    title = detail?.topic ?: "Review",
                    onBack = onBack,
                    actions = {
                        if (detail != null) {
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, buildReportText(detail!!))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share report"))
                            }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share Report")
                            }
                        }
                    }
                )
                BannerAdView()
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val attempt = detail
            if (attempt == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AttemptDetailContent(attempt = attempt)
            }
        }
    }
}

private fun buildReportText(attempt: ExamAttemptSummary): String {
    val header = buildString {
        append("ExamAI Report — ${attempt.exam} • ${attempt.subject} • ${attempt.topic}\n")
        append("Score: ${attempt.score}/${attempt.total}\n")
        append("Date: ${formatDateTime(attempt.startTime)}\n")
        append("Duration: ${formatDuration(attempt.durationMillis)}\n\n")
    }
    val questions = attempt.questions.mapIndexed { index, question ->
        val userAnswer = attempt.answers.getOrNull(index) ?: -1
        val userAnswerText = question.options.getOrNull(userAnswer) ?: "Not answered"
        val correctAnswerText = question.options.getOrNull(question.correct).orEmpty()
        buildString {
            append("Q${index + 1}. ${question.question}\n")
            append("Your answer: $userAnswerText\n")
            append("Correct answer: $correctAnswerText\n")
            append("Explanation: ${question.explanation}\n")
        }
    }.joinToString("\n")
    return header + questions
}

@Composable
private fun AttemptDetailContent(attempt: ExamAttemptSummary) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SummaryHeader(attempt) }
        itemsIndexed(attempt.questions) { index, question ->
            StaggeredEntrance(index = index) {
                QuestionReviewCard(
                    index = index,
                    question = question,
                    userAnswer = attempt.answers.getOrNull(index) ?: -1
                )
            }
        }
    }
}

@Composable
private fun SummaryHeader(attempt: ExamAttemptSummary) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${attempt.exam} • ${attempt.subject}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "${attempt.score} / ${attempt.total}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = formatDateTime(attempt.startTime),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "  •  ${formatDuration(attempt.durationMillis)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun QuestionReviewCard(index: Int, question: Question, userAnswer: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Question ${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            question.options.forEachIndexed { optionIndex, option ->
                ReviewOptionRow(
                    text = option,
                    isCorrectOption = optionIndex == question.correct,
                    isUserChoice = optionIndex == userAnswer
                )
            }

            if (userAnswer == -1) {
                Text(
                    text = "Not answered",
                    style = MaterialTheme.typography.bodySmall,
                    color = WrongRed,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewOptionRow(text: String, isCorrectOption: Boolean, isUserChoice: Boolean) {
    val containerColor = when {
        isCorrectOption -> CorrectGreen.copy(alpha = 0.15f)
        isUserChoice -> WrongRed.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isCorrectOption -> CorrectGreen
        isUserChoice -> WrongRed
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (isCorrectOption || isUserChoice) {
                Icon(
                    imageVector = if (isCorrectOption) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isCorrectOption) CorrectGreen else WrongRed
                )
            }
        }
    }
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(millis)

private fun formatDuration(millis: Long?): String {
    if (millis == null) return "In progress"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}

private val samplePreviewAttempt = ExamAttemptSummary(
    id = 1,
    exam = "JEE",
    subject = "Physics",
    topic = "Laws of Motion",
    startTime = System.currentTimeMillis() - 600000,
    endTime = System.currentTimeMillis(),
    questions = listOf(
        Question(
            question = "What is the SI unit of force?",
            options = listOf("Newton", "Joule", "Watt", "Pascal"),
            correct = 0,
            explanation = "Newton is the SI unit of force. F = ma."
        )
    ),
    answers = listOf(1),
    isCompleted = true
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AttemptDetailContentPreview() {
    AIStudyAssistantTheme {
        AttemptDetailContent(attempt = samplePreviewAttempt)
    }
}
