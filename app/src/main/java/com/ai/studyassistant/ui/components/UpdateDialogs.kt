package com.ai.studyassistant.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme

@Composable
fun UpdateAvailableDialog(onUpdate: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null) },
        title = { Text("Update available") },
        text = { Text("A new version of ExamAI is available with new features and improvements. Update now for the best experience.") },
        confirmButton = {
            TextButton(onClick = onUpdate) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdateReadyDialog(onRestart: () -> Unit, onLater: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null) },
        title = { Text("Update downloaded") },
        text = { Text("The update has finished downloading. Restart now to finish installing it.") },
        confirmButton = {
            TextButton(onClick = onRestart) { Text("Restart") }
        },
        dismissButton = {
            TextButton(onClick = onLater) { Text("Later") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun UpdateAvailableDialogPreview() {
    AIStudyAssistantTheme {
        UpdateAvailableDialog(onUpdate = {}, onCancel = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateReadyDialogPreview() {
    AIStudyAssistantTheme {
        UpdateReadyDialog(onRestart = {}, onLater = {})
    }
}
