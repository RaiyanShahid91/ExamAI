package com.ai.studyassistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.Badge
import com.ai.studyassistant.data.model.InsightsData
import com.ai.studyassistant.data.model.StudyDays
import com.ai.studyassistant.data.model.SubjectStat
import com.ai.studyassistant.data.model.TopicStat
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.ScreenIntro
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.util.HistoryExporter
import com.ai.studyassistant.viewmodel.InsightsViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

private const val HEATMAP_WEEKS = 10

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBack: () -> Unit,
    onPracticeTopic: (exam: String, subject: String, topic: String) -> Unit
) {
    val data by viewModel.insights.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    InsightsContent(
        data = data,
        onBack = onBack,
        onPracticeTopic = onPracticeTopic,
        onExport = {
            scope.launch {
                val uri = HistoryExporter.exportCsv(context)
                if (uri != null) {
                    AnalyticsManager.logEvent("history_exported")
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.insights_export_chooser)))
                }
            }
        }
    )
}

@Composable
private fun InsightsContent(
    data: InsightsData,
    onBack: () -> Unit,
    onPracticeTopic: (String, String, String) -> Unit,
    onExport: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = stringResource(R.string.insights_insights), onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScreenIntro(stringResource(R.string.insights_progress_glance), stringResource(R.string.insights_see_whats_working)) }
            item { OverviewRow(data) }
            item { StreakCard(data) }
            item { WeakTopicsCard(data.weakTopics, onPracticeTopic) }
            if (data.subjectStats.isNotEmpty()) item { SubjectCard(data.subjectStats) }
            item { BadgesCard(data.badges) }
            item {
                OutlinedButton(
                    onClick = onExport,
                    enabled = data.quizzesCompleted > 0,
                    modifier = Modifier.fillMaxWidth().bounceClick()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.insights_export_my_history_csv), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewRow(data: InsightsData) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile("${data.quizzesCompleted}", stringResource(R.string.insights_quizzes), Modifier.weight(1f))
        StatTile("${data.questionsAnswered}", stringResource(R.string.insights_questions), Modifier.weight(1f))
        StatTile(if (data.questionsAnswered == 0) "–" else "${(data.accuracy * 100).toInt()}%", stringResource(R.string.insights_accuracy), Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun StreakCard(data: InsightsData) {
    SectionCard(stringResource(R.string.insights_study_streak)) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("${data.currentStreak}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.insights_current_days), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                Text("${data.bestStreak}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.insights_best_days), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        StudyHeatmap(data.studyDays)
        Text(stringResource(R.string.insights_last_weeks_each_square_is, HEATMAP_WEEKS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StudyHeatmap(studyDays: Set<Long>) {
    val today = StudyDays.epochDay(System.currentTimeMillis())
    // Monday-based weekday index (epoch day 0 was a Thursday).
    val todayDow = Math.floorMod(today + 3, 7L)
    val firstMonday = today - todayDow - 7L * (HEATMAP_WEEKS - 1)
    val studiedColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (week in 0 until HEATMAP_WEEKS) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dow in 0 until 7) {
                    val day = firstMonday + week * 7L + dow
                    val shape = RoundedCornerShape(4.dp)
                    when {
                        day > today -> Box(Modifier.size(22.dp))
                        else -> Box(
                            Modifier
                                .size(22.dp)
                                .background(if (day in studyDays) studiedColor else emptyColor, shape)
                                .then(if (day == today) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, shape) else Modifier)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeakTopicsCard(topics: List<TopicStat>, onPractice: (String, String, String) -> Unit) {
    SectionCard(stringResource(R.string.insights_needs_practice)) {
        if (topics.isEmpty()) {
            Text(stringResource(R.string.insights_no_weak_spots_yet_answer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            topics.forEach { topic ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(topic.topic, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${topic.exam} · ${topic.subject} · ${(topic.accuracy * 100).toInt()}% (${topic.correct}/${topic.total})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { onPractice(topic.exam, topic.subject, topic.topic) }, modifier = Modifier.bounceClick()) {
                        Text(stringResource(R.string.insights_practise))
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(stats: List<SubjectStat>) {
    SectionCard(stringResource(R.string.insights_accuracy_by_subject)) {
        stats.forEach { stat ->
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stat.subject, style = MaterialTheme.typography.bodyMedium)
                    Text("${(stat.accuracy * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(progress = { stat.accuracy }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun BadgesCard(badges: List<Badge>) {
    SectionCard(stringResource(R.string.insights_badges_title, badges.count { it.earned }, badges.size)) {
        badges.forEach { badge ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (badge.earned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = if (badge.earned) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = if (badge.earned) stringResource(R.string.insights_badge_earned) else stringResource(R.string.insights_badge_locked),
                        tint = if (badge.earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column {
                    Text(stringResource(badge.titleRes), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(badge.descriptionRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    AIStudyAssistantTheme {
        val today = StudyDays.epochDay(System.currentTimeMillis())
        InsightsContent(
            data = InsightsData(
                quizzesCompleted = 12, questionsAnswered = 120, correctAnswers = 84,
                currentStreak = 3, bestStreak = 6,
                studyDays = setOf(today, today - 1, today - 2, today - 9, today - 10),
                weakTopics = listOf(TopicStat("JEE", "Physics", "Optics", 3, 10)),
                subjectStats = listOf(SubjectStat("Physics", 30, 50)),
                badges = com.ai.studyassistant.data.model.Badges.evaluate(12, 120, 0.7f, 6, 0)
            ),
            onBack = {}, onPracticeTopic = { _, _, _ -> }, onExport = {}
        )
    }
}
