package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme

@Composable
fun TopicScreen(
    exam: String,
    subject: String,
    onBack: () -> Unit,
    onTopicSelected: (String) -> Unit
) {
    val topics = ExamData.topicsFor(exam, subject)

    Scaffold(
        topBar = { BackTopBar(title = subject, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(topics) { topic ->
                TopicChip(topic = topic, onClick = { onTopicSelected(topic) })
            }
        }
    }
}

@Composable
private fun TopicChip(topic: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TopicScreenPreview() {
    AIStudyAssistantTheme {
        TopicScreen(exam = "JEE", subject = "Physics", onBack = {}, onTopicSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicChipPreview() {
    AIStudyAssistantTheme {
        TopicChip(topic = "Mechanics", onClick = {})
    }
}
