package com.ai.studyassistant.ui.screens

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.model.SubjectStat
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.StaggeredEntrance
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onResume: (Long) -> Unit,
    onOpenDetail: (Long) -> Unit
) {
    val attempts by viewModel.attempts.collectAsState()
    val subjectStats by viewModel.subjectStats.collectAsState()

    HistoryContent(
        attempts = attempts,
        subjectStats = subjectStats,
        onBack = onBack,
        onCardClick = { attempt ->
            if (attempt.isCompleted) onOpenDetail(attempt.id) else onResume(attempt.id)
        }
    )
}

@Composable
private fun HistoryContent(
    attempts: List<ExamAttemptSummary>,
    subjectStats: List<SubjectStat> = emptyList(),
    onBack: () -> Unit,
    onCardClick: (ExamAttemptSummary) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = "History", onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        if (attempts.isEmpty()) {
            EmptyHistory(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (subjectStats.isNotEmpty()) {
                    item { SubjectAccuracySummary(stats = subjectStats) }
                }
                itemsIndexed(attempts, key = { _, attempt -> attempt.id }) { index, attempt ->
                    StaggeredEntrance(index = index) {
                        AttemptCard(attempt = attempt, onClick = { onCardClick(attempt) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectAccuracySummary(stats: List<SubjectStat>) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Accuracy by subject",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            stats.forEach { stat ->
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stat.subject, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${(stat.accuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { stat.accuracy },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No exams taken yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Attempts you start or finish will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AttemptCard(attempt: ExamAttemptSummary, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = attempt.topic,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${attempt.exam} • ${attempt.subject}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDateTime(attempt.startTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (attempt.isCompleted) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${attempt.score} / ${attempt.total}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CorrectGreen
                        )
                        Text(
                            text = formatDuration(attempt.durationMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "View details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${attempt.answeredCount} of ${attempt.total} answered",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "In progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Resume")
                    }
                }
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

private val sampleCompleted = ExamAttemptSummary(
    id = 1,
    exam = "JEE",
    subject = "Physics",
    topic = "Laws of Motion",
    startTime = System.currentTimeMillis() - 600000,
    endTime = System.currentTimeMillis(),
    questions = List(10) {
        Question(question = "Q$it", options = listOf("A", "B", "C", "D"), correct = 0, explanation = "Because.")
    },
    answers = List(10) { 0 },
    isCompleted = true
)

private val sampleInProgress = sampleCompleted.copy(
    id = 2,
    topic = "Thermodynamics",
    endTime = null,
    answers = List(10) { if (it < 3) 0 else -1 },
    isCompleted = false
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryContentPreview() {
    AIStudyAssistantTheme {
        HistoryContent(attempts = listOf(sampleCompleted, sampleInProgress), onBack = {}, onCardClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryContentEmptyPreview() {
    AIStudyAssistantTheme {
        HistoryContent(attempts = emptyList(), onBack = {}, onCardClick = {})
    }
}
