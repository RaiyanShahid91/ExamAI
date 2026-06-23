package com.ai.studyassistant.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.DeepPurple

private const val UPSC_URL = "https://upsc.gov.in"

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    AboutScreenContent(
        onBack = onBack,
        onUpscWebsiteClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UPSC_URL)))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreenContent(
    onBack: () -> Unit,
    onUpscWebsiteClick: () -> Unit
) {
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { BackTopBar(title = "About", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppInfoSection()
            DisclaimerCardAbout()
            OfficialSourcesCard()
            FeaturesCard()
            Button(
                onClick = { showPrivacyPolicy = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }
            /*OutlinedButton(
                onClick = onUpscWebsiteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Official UPSC Website")
            }*/
        }
    }

    if (showPrivacyPolicy) {
        ModalBottomSheet(onDismissRequest = { showPrivacyPolicy = false }) {
            PrivacyPolicyBody()
        }
    }
}

@Composable
private fun AppInfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = DeepPurple.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = DeepPurple,
                modifier = Modifier.padding(16.dp)
            )
        }
        Text(
            text = "ExamAI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Version 1.0.2",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        /*Text(
            text = "Developer: Raiyan Shahid",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )*/
    }
}

@Composable
private fun DisclaimerCardAbout() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF3CD),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardTitleRow(icon = Icons.Default.Info, title = "DISCLAIMER", color = Color(0xFF856404))
            Text(
                text = "ExamAI is an independent study app developed by an " +
                        "individual developer. This app is NOT affiliated with, " +
                        "endorsed by, or representing any government entity " +
                        "including UPSC, NTA or any official exam authority. " +
                        "This is NOT an official government app.",
                color = Color(0xFF856404),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun OfficialSourcesCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFD1ECF1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Official Sources",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C5460),
                fontSize = 14.sp
            )
            Text(
                text = "• JEE: jeemain.nta.nic.in\n" +
                        "• NEET: neet.nta.nic.in\n" +
                        "• UPSC: upsc.gov.in",
                color = Color(0xFF0C5460),
                fontSize = 12.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun FeaturesCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "What ExamAI Offers",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "✅ 200+ MCQ questions for JEE, NEET & UPSC\n" +
                        "✅ Live questions from Open Trivia DB\n" +
                        "✅ Study notes from Wikipedia\n" +
                        "✅ Works offline after first use\n" +
                        "✅ 100% Free forever\n" +
                        "✅ No login required\n" +
                        "✅ No ads",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun CardTitleRow(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "  $title",
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AboutScreenPreview() {
    AIStudyAssistantTheme {
        AboutScreenContent(onBack = {}, onUpscWebsiteClick = {})
    }
}
