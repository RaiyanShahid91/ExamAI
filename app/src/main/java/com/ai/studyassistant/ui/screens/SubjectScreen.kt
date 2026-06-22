package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.model.Subject
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.DeepPurple

@Composable
fun SubjectScreen(
    exam: String,
    onBack: () -> Unit,
    onSubjectSelected: (String) -> Unit
) {
    val subjects = ExamData.subjectsFor(exam)

    Scaffold(
        topBar = { BackTopBar(title = "$exam Subjects", onBack = onBack) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(subjects) { subject ->
                SubjectCard(subject = subject, onClick = { onSubjectSelected(subject.name) })
            }
        }
    }
}

@Composable
private fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = subject.color.copy(alpha = 0.12f),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = subject.icon,
                contentDescription = subject.name,
                tint = subject.color,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.87f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SubjectScreenPreview() {
    AIStudyAssistantTheme {
        SubjectScreen(exam = "JEE", onBack = {}, onSubjectSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SubjectCardPreview() {
    AIStudyAssistantTheme {
        SubjectCard(subject = Subject("Physics", Icons.Default.Science, DeepPurple), onClick = {})
    }
}
