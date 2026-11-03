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
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun UpdateAvailableDialog(onUpdate: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null) },
        title = { Text(stringResource(R.string.update_update_available)) },
        text = { Text(stringResource(R.string.update_a_new_version_of_examai)) },
        confirmButton = {
            TextButton(onClick = onUpdate) { Text(stringResource(R.string.update_update)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.reminders_cancel)) }
        }
    )
}

@Composable
fun UpdateReadyDialog(onRestart: () -> Unit, onLater: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null) },
        title = { Text(stringResource(R.string.update_update_downloaded)) },
        text = { Text(stringResource(R.string.update_the_update_has_finished_downloading)) },
        confirmButton = {
            TextButton(onClick = onRestart) { Text(stringResource(R.string.update_restart)) }
        },
        dismissButton = {
            TextButton(onClick = onLater) { Text(stringResource(R.string.update_later)) }
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
