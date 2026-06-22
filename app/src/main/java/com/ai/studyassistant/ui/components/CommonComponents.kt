package com.ai.studyassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.Cyan
import com.ai.studyassistant.ui.theme.DeepPurple

@Composable
fun GradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors))
    ) {
        content()
    }
}

@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = colors
    )
}

val CardCornerShape = RoundedCornerShape(16.dp)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GradientBackgroundPreview() {
    AIStudyAssistantTheme {
        GradientBackground(colors = listOf(DeepPurple, Cyan)) {
            Text(text = "Gradient content", color = Color.White, modifier = Modifier.padding(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BackTopBarPreview() {
    AIStudyAssistantTheme {
        BackTopBar(title = "Physics", onBack = {})
    }
}
