package com.ai.studyassistant.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.notification.DailyReminderWorker
import com.ai.studyassistant.notification.ReminderPreferences
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandNavy
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAiInfoClick: () -> Unit
) {
    val context = LocalContext.current
    val reminderPreferences = remember { ReminderPreferences(context) }
    val reminderEnabled by reminderPreferences.isEnabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    fun enableReminders() {
        scope.launch { reminderPreferences.setEnabled(true) }
        DailyReminderWorker.schedule(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableReminders()
    }

    SettingsContent(
        reminderEnabled = reminderEnabled,
        onReminderToggle = { enabled ->
            if (!enabled) {
                scope.launch { reminderPreferences.setEnabled(false) }
                DailyReminderWorker.cancel(context)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                enableReminders()
            }
        },
        onBack = onBack,
        onAboutClick = onAboutClick,
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick,
        onAiInfoClick = onAiInfoClick
    )
}

@Composable
private fun SettingsContent(
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAiInfoClick: () -> Unit
) {
    Scaffold(
        topBar = { BackTopBar(title = "Settings", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SettingsHeader() }
            item { ReminderToggleCard(enabled = reminderEnabled, onToggle = onReminderToggle) }
            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "About ExamAI",
                    subtitle = "App info, disclaimer, official sources",
                    onClick = onAboutClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "How AI Picks Your Questions",
                    subtitle = "Where the questions come from",
                    onClick = onAiInfoClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = onPrivacyClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Gavel,
                    title = "Terms & Conditions",
                    subtitle = "Rules for using ExamAI",
                    onClick = onTermsClick
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = BrandNavy.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = BrandNavy,
                modifier = Modifier.padding(12.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "ExamAI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Smart Study Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReminderToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Get a daily nudge to keep your streak going",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    AIStudyAssistantTheme {
        SettingsContent(
            reminderEnabled = true,
            onReminderToggle = {},
            onBack = {},
            onAboutClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
            onAiInfoClick = {}
        )
    }
}
