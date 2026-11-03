package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.SupportContactCard

/** Display the same policy text shipped as PRIVACY_POLICY.md, including all reminder disclosures. */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sections = remember(context) {
        context.assets.open("privacy_policy.md").bufferedReader().use { it.readText() }.split("\n## ")
    }
    Scaffold(topBar = { BackTopBar("Privacy policy", onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(sections) { index, section ->
                val heading = section.substringBefore('\n').removePrefix("# ")
                val body = section.substringAfter('\n', "").trim()
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    SelectionContainer {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(heading, style = if (index == 0) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text(remember(body) { policyBody(body) }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item { SupportContactCard() }
        }
    }
}

/** Render the policy's inline section labels without exposing Markdown punctuation. */
private fun policyBody(body: String) = buildAnnotatedString {
    var cursor = 0
    Regex("\\*\\*(.+?)\\*\\*").findAll(body).forEach { match ->
        append(body.substring(cursor, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(match.groupValues[1]) }
        cursor = match.range.last + 1
    }
    append(body.substring(cursor))
}
