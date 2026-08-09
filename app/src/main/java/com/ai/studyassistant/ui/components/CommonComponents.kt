package com.ai.studyassistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandTeal
import com.ai.studyassistant.ui.theme.BrandNavy

@Composable
fun GradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    content: @Composable () -> Unit
) {
    val brush = if (animated) {
        val transition = rememberInfiniteTransition(label = "gradientShift")
        val sweep by transition.animateFloat(
            initialValue = -600f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
            label = "gradientSweep"
        )
        Brush.linearGradient(
            colors = colors,
            start = Offset(sweep, 0f),
            end = Offset(sweep + 1200f, 1200f)
        )
    } else {
        Brush.linearGradient(colors)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}

@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { actions() },
        colors = colors
    )
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        color = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "You're offline — showing cached questions",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GradientBackgroundPreview() {
    AIStudyAssistantTheme {
        GradientBackground(colors = listOf(BrandNavy, BrandTeal)) {
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
