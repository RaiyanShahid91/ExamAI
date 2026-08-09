package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandNavy

private data class TermsSection(val title: String, val body: String)

private val termsSections = listOf(
    TermsSection(
        title = "Acceptance of Terms",
        body = "By downloading, installing, or using ExamAI – Smart Study Assistant (\"the App\"), " +
                "you agree to be bound by these Terms & Conditions. If you do not agree, please do " +
                "not use the App."
    ),
    TermsSection(
        title = "What ExamAI Is",
        body = "ExamAI is an independent, AI/algorithm-curated practice app for competitive exam " +
                "preparation (JEE, NEET, UPSC, SSC, Banking, CAT, CDS/NDA, and more). It is built " +
                "and maintained by an individual developer.\n\n" +
                "ExamAI is NOT affiliated with, endorsed by, or representing any coaching center, " +
                "tutor, teacher, publisher, or official exam authority (UPSC, NTA, SSC, IBPS, or " +
                "otherwise)."
    ),
    TermsSection(
        title = "License to Use",
        body = "We grant you a limited, non-exclusive, non-transferable, revocable license to use " +
                "the App for personal, non-commercial exam preparation. You may not copy, resell, " +
                "reverse-engineer, or redistribute the App or its content."
    ),
    TermsSection(
        title = "Accuracy of Content — Important Disclaimer",
        body = "Questions in the App are AI/algorithm-curated and blended from free public sources " +
                "(Open Trivia Database, Wikipedia, NCERT) plus a hand-picked local question bank. " +
                "While we aim for accuracy, this content:\n" +
                "• May contain errors, outdated facts, or inaccuracies\n" +
                "• Is NOT official exam material and is NOT guaranteed to match any real exam's " +
                "syllabus, pattern, or difficulty\n" +
                "• Should be used only as supplementary practice, not as your sole source of " +
                "exam preparation\n\n" +
                "Always cross-check important facts against official sources before relying on them."
    ),
    TermsSection(
        title = "Ad-Supported Free Access",
        body = "ExamAI is free to use. To keep it free, the App displays advertisements " +
                "(via Google AdMob) at non-intrusive points such as list screens and after " +
                "completing a quiz. Ads are never shown during active question-answering. See our " +
                "Privacy Policy for details on how advertising works in the App."
    ),
    TermsSection(
        title = "No Accounts, No Purchases",
        body = "ExamAI does not require account creation or login, and does not currently sell any " +
                "in-app products or subscriptions."
    ),
    TermsSection(
        title = "No Warranty & Limitation of Liability",
        body = "The App is provided \"as is\" without warranties of any kind. We are not liable for " +
                "any exam results, decisions, or outcomes based on your use of the App, nor for any " +
                "damages arising from its use or inability to use it."
    ),
    TermsSection(
        title = "User Conduct",
        body = "You agree not to misuse the App, attempt to disrupt its functioning, or use it for " +
                "any unlawful purpose."
    ),
    TermsSection(
        title = "Changes to These Terms",
        body = "We may update these Terms from time to time. Continued use of the App after changes " +
                "means you accept the updated Terms."
    ),
    TermsSection(
        title = "Contact Us",
        body = "Questions about these Terms? Contact us at raiyannshahid@gmail.com."
    )
)

@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { BackTopBar(title = "Terms & Conditions", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(termsSections) { section ->
                TermsSectionCard(section)
            }
        }
    }
}

@Composable
private fun TermsSectionCard(section: TermsSection) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = section.body,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TermsAndConditionsScreenPreview() {
    AIStudyAssistantTheme {
        TermsAndConditionsScreen(onBack = {})
    }
}
