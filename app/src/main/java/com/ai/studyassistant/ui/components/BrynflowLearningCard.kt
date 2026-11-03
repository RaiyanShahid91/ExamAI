package com.ai.studyassistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.ui.util.AppLinks
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun BrynflowLearningCard(source: String) {
    val context = LocalContext.current
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(stringResource(R.string.brynflow_learning_learn_on_brynflow), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.brynflow_learning_continue_your_exam_preparation), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {
                AnalyticsManager.logEvent("brynflow_exam_prep_opened", mapOf("source" to source))
                AppLinks.openExamPrep(context)
            }) {
                Text(stringResource(R.string.brynflow_learning_explore_exam_prep))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp))
            }
            Text(stringResource(R.string.brynflow_learning_brynflow_com_exams_opens_in), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun SupportContactCard() {
    val context = LocalContext.current
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.brynflow_learning_we_re_here_to_help), style = MaterialTheme.typography.titleMedium)
            Text(AppLinks.SUPPORT_EMAIL, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = { AppLinks.contactSupport(context) }) { Text(stringResource(R.string.brynflow_learning_contact_support)) }
        }
    }
}
