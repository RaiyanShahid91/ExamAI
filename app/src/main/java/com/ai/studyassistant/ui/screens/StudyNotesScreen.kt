package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.StudyNote
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.NotesUiState
import com.ai.studyassistant.viewmodel.NotesViewModel

private val quickTopics = listOf(
    "Photosynthesis", "Newton's Laws of Motion", "Indian Constitution",
    "French Revolution", "Periodic Table", "Cell Biology"
)

@Composable
fun StudyNotesScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    StudyNotesContent(
        uiState = uiState,
        onSearch = { topic -> viewModel.fetchNote(topic) },
        onToggleSave = { viewModel.toggleSave() },
        onBack = onBack
    )
}

@Composable
private fun StudyNotesContent(
    uiState: NotesUiState,
    onSearch: (String) -> Unit,
    onToggleSave: () -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { BackTopBar(title = "Study Notes", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search any topic") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { if (query.isNotBlank()) onSearch(query.trim()) }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            Text(
                text = "Quick topics",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickTopics) { topic ->
                    AssistChip(
                        onClick = {
                            query = topic
                            onSearch(topic)
                        },
                        label = { Text(topic) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                uiState.note != null -> {
                    val note = uiState.note!!
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onToggleSave) {
                                    Icon(
                                        imageVector = if (note.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save note"
                                    )
                                }
                            }
                            Text(
                                text = note.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.savedNotes.isNotEmpty()) {
                Text(
                    text = "Saved for offline",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 28.dp, bottom = 8.dp)
                )
                uiState.savedNotes.forEach { saved ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        onClick = { onSearch(saved.topic) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = saved.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private val sampleNote = StudyNote(
    topic = "Photosynthesis",
    title = "Photosynthesis",
    summary = "Photosynthesis is the process by which green plants and some other organisms use sunlight to synthesize nutrients from carbon dioxide and water, generating oxygen as a byproduct.",
    isSaved = true
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(
                note = sampleNote,
                savedNotes = listOf(sampleNote, sampleNote.copy(topic = "Newton's Laws of Motion", title = "Newton's Laws of Motion"))
            ),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentLoadingPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(isLoading = true),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentErrorPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(error = "Couldn't fetch notes for this topic."),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}
