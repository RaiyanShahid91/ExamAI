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
                "The app selects and mixes available practice content automatically. The bundled " +
                "banks are organized by subject; topic selection and difficulty do not guarantee " +
                "strict matching for every question. This selection process is what we mean by " +
                "\"AI/algorithm-curated\"; the app does not send your saved answers to a generative-AI service."
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
        body = "Bundled questions, cached content and saved notes let you keep studying offline. " +
                "Fresh questions and note searches need internet. Ads, Firebase analytics and crash " +
                "reporting, and Google Play update checks can also use online services. Your study " +
                "records are saved locally; see the Privacy Policy for third-party data processing."
    ),
    AiInfoSection(
        title = "Local study data and online services",
        body = "We do not run a study-account or cloud-sync server. Flashcards and progress insights " +
                "are built on your device from saved questions and attempts. Public content providers " +
                "receive content requests, while Google AdMob, Firebase Analytics and Crashlytics " +
                "process advertising, usage and diagnostic information. Local storage does not mean " +
                "that no app activity is sent to third parties. Read the Privacy Policy for details."
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
                color = MaterialTheme.colorScheme.primary,
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
