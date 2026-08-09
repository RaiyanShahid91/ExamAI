package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.unit.sp
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandNavy

private data class AiInfoSection(val title: String, val body: String)

private val aiInfoSections = listOf(
    AiInfoSection(
        title = "Where your questions come from",
        body = "Every quiz in ExamAI is built by blending questions from:\n\n" +
                "• A hand-picked local question bank stored inside the App\n" +
                "• Open Trivia Database (opentdb.com) — a free, public question API\n" +
                "• Wikipedia — for topic summaries in Study Notes\n" +
                "• NCERT — free public textbook content\n\n" +
                "The App picks, filters, and mixes these automatically based on the exam, subject, " +
                "topic, and difficulty you choose — that selection process is what we mean by " +
                "\"AI/algorithm-curated.\""
    ),
    AiInfoSection(
        title = "Not from any coaching center or teacher",
        body = "ExamAI is an independent app built by an individual developer. No coaching " +
                "institute, tutor, teacher, publisher, or official exam authority (UPSC, NTA, SSC, " +
                "IBPS, or otherwise) has written, reviewed, or endorsed this content. Treat it as " +
                "supplementary practice, not official exam material."
    ),
    AiInfoSection(
        title = "Offline-first by design",
        body = "Questions you've already loaded are cached on your device, so you can keep " +
                "practicing without an internet connection. Internet access is only used to fetch " +
                "fresh or additional questions from the public sources above — everything else " +
                "(your answers, scores, bookmarks, and history) is generated and stored entirely " +
                "on your device."
    ),
    AiInfoSection(
        title = "No servers of our own",
        body = "ExamAI does not run a backend server. There is nowhere for your activity to be " +
                "stored \"in the cloud\" by us, because we don't operate any cloud infrastructure. " +
                "Your device talks directly to the public APIs listed above to fetch content — " +
                "nothing is routed through or logged by us."
    )
)

@Composable
fun AiInfoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { BackTopBar(title = "How AI Picks Your Questions", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { AiInfoHeader() }
            items(aiInfoSections) { section ->
                AiInfoSectionCard(section)
            }
        }
    }
}

@Composable
private fun AiInfoHeader() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = "Purely AI-curated practice content — not affiliated with any coaching " +
                        "center, teacher, or exam authority.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AiInfoSectionCard(section: AiInfoSection) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = section.body,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AiInfoScreenPreview() {
    AIStudyAssistantTheme {
        AiInfoScreen(onBack = {})
    }
}
