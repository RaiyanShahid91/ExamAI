package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.StaggeredEntrance
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

private val DIFFICULTIES = listOf("Any", "Easy", "Medium", "Hard")

@androidx.annotation.StringRes
private fun difficultyLabel(difficulty: String) = when (difficulty) {
    "Easy" -> R.string.difficulty_easy
    "Medium" -> R.string.difficulty_medium
    "Hard" -> R.string.difficulty_hard
    else -> R.string.difficulty_any
}

@Composable
fun TopicScreen(
    exam: String,
    subject: String,
    onBack: () -> Unit,
    onTopicSelected: (String, String?, Boolean) -> Unit
) {
    val topics = ExamData.topicsFor(exam, subject)
    var selectedDifficulty by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("Any") }
    var timedMode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = subject, onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { com.ai.studyassistant.ui.components.ScreenIntro(stringResource(R.string.topic_one_at_a_time), stringResource(R.string.topic_choose_challenge)) }
            item {
                DifficultyFilterRow(
                    selected = selectedDifficulty,
                    onSelect = {
                        selectedDifficulty = it
                        AnalyticsManager.logEvent("difficulty_filter_selected", mapOf("difficulty" to it))
                    }
                )
            }
            item {
                TimedModeToggle(
                    checked = timedMode,
                    onCheckedChange = {
                        timedMode = it
                        AnalyticsManager.logEvent("timed_mode_toggled", mapOf("enabled" to it))
                    }
                )
            }
            itemsIndexed(topics) { index, topic ->
                StaggeredEntrance(index = index) {
                    TopicChip(
                        topic = topic,
                        onClick = {
                            val difficulty = selectedDifficulty.takeIf { it != "Any" }
                            onTopicSelected(topic, difficulty, timedMode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyFilterRow(selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        items(DIFFICULTIES.size) { index ->
            val difficulty = DIFFICULTIES[index]
            FilterChip(
                selected = selected == difficulty,
                onClick = { onSelect(difficulty) },
                label = { Text(stringResource(difficultyLabel(difficulty))) }
            )
        }
    }
}

@Composable
private fun TimedModeToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.topic_timed_mode), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun TopicChip(topic: String, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topic,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TopicScreenPreview() {
    AIStudyAssistantTheme {
        TopicScreen(exam = "JEE", subject = "Physics", onBack = {}, onTopicSelected = { _, _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicChipPreview() {
    AIStudyAssistantTheme {
        TopicChip(topic = "Mechanics", onClick = {})
    }
}
