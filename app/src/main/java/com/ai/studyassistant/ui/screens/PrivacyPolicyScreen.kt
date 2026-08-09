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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandNavy

private data class PolicySection(val title: String, val body: String)

private val policySections = listOf(
    PolicySection(
        title = "App Information",
        body = "App Name: ExamAI – Smart Study Assistant\n" +
                "Developer: Raiyan Shahid\n" +
                "Contact Email: raiyannshahid@gmail.com"
    ),
    PolicySection(
        title = "Introduction",
        body = "Welcome to ExamAI – Smart Study Assistant (\"we\", \"our\", or \"us\"). " +
                "This Privacy Policy explains how our Android application (\"ExamAI\" or " +
                "\"the App\") handles your information when you use it on your Android device.\n\n" +
                "We are committed to protecting your privacy. ExamAI is designed with a " +
                "privacy-first approach — we do not collect, store, or share any personal " +
                "information about you.\n\n" +
                "Please read this policy carefully. By using ExamAI, you agree to the terms " +
                "described in this Privacy Policy."
    ),
    PolicySection(
        title = "Information We Do NOT Collect",
        body = "ExamAI itself does not collect any of the following:\n" +
                "❌ Name, email address, or phone number\n" +
                "❌ Location or GPS data\n" +
                "❌ Camera or microphone access\n" +
                "❌ Contacts or call logs\n" +
                "❌ Photos or media files\n" +
                "❌ Financial or payment information\n" +
                "❌ Login credentials or passwords\n" +
                "❌ Browsing history\n" +
                "❌ Any personally identifiable information (PII)\n\n" +
                "The only exceptions are the advertising ID (used by Google AdMob — see " +
                "\"Advertising\") and an anonymous Firebase installation identifier (used for " +
                "crash reporting and app analytics — see \"Analytics & Crash Reporting\"). " +
                "Neither is tied to your name or any other personal identity."
    ),
    PolicySection(
        title = "Information Stored Locally On Your Device",
        body = "ExamAI stores the following data only on your device using Room Database " +
                "(local storage). This data never leaves your phone:\n" +
                "• Quiz scores — track your progress\n" +
                "• Cached questions — offline mode support\n" +
                "• Study notes — save for later reading\n" +
                "• App preferences — dark mode, settings\n\n" +
                "You can delete all this data at any time by uninstalling the app."
    ),
    PolicySection(
        title = "Internet Permission & Third Party APIs",
        body = "ExamAI requires internet access to fetch fresh study content. We use the " +
                "following free, public APIs:\n\n" +
                "Open Trivia Database (opentdb.com)\n" +
                "• What we fetch: MCQ questions for practice\n" +
                "• What we send: Only the topic/category name\n\n" +
                "Wikipedia REST API (wikipedia.org)\n" +
                "• What we fetch: Topic summaries for study notes\n" +
                "• What we send: Only the topic name you searched\n\n" +
                "NCERT Website (ncert.nic.in)\n" +
                "• What we fetch: Free textbook content\n" +
                "• What we send: Only chapter/subject names\n\n" +
                "Important: We do not send any personal information to any of these APIs. " +
                "Only topic or subject names are sent as part of the content request."
    ),
    PolicySection(
        title = "No User Accounts",
        body = "ExamAI does not require you to:\n" +
                "• Create an account\n" +
                "• Log in with Google, Facebook, or any service\n" +
                "• Provide an email address\n" +
                "• Verify your phone number\n\n" +
                "You can use the full app anonymously from the moment you install it."
    ),
    PolicySection(
        title = "Advertising",
        body = "ExamAI shows ads via Google AdMob to keep the app free, at non-intrusive " +
                "points (list screens and after finishing a quiz) — never during active " +
                "question-answering.\n\n" +
                "AdMob may collect and use your device's advertising ID and general technical " +
                "data (device type, general location from IP, ad interaction data) to serve " +
                "and measure ads, as described in Google's own Ads Privacy & Terms " +
                "(policies.google.com/technologies/ads). We do not receive or store this data " +
                "ourselves — it is processed by Google's ad infrastructure.\n\n" +
                "Where required by law (e.g. EEA/UK), ExamAI asks for your consent to " +
                "personalized ads via Google's User Messaging Platform before loading ads. " +
                "You can decline personalized ads and still see non-personalized ads. You can " +
                "manage ad personalization for your device in your Google Account settings."
    ),
    PolicySection(
        title = "Analytics & Crash Reporting",
        body = "ExamAI uses Google Firebase Analytics and Firebase Crashlytics to understand " +
                "how the app is used and to catch crashes and errors so we can fix them.\n\n" +
                "What this includes: screens viewed, in-app actions (e.g. which exam/subject/" +
                "topic you practice, quiz completion and scores, bookmark and note actions), " +
                "general device/app info (device model, OS version, app version, approximate " +
                "country from IP), and crash/error reports with stack traces.\n\n" +
                "What this does NOT include: your name, email address, phone number, or any " +
                "other information that identifies you personally. Firebase assigns an " +
                "anonymous installation identifier to your device, not your real identity.\n\n" +
                "This data is processed by Google under Firebase's own privacy terms " +
                "(firebase.google.com/support/privacy). We use it only to improve app " +
                "stability and understand feature usage — never to build an advertising " +
                "profile of you outside of the AdMob usage described above."
    ),
    PolicySection(
        title = "Children's Privacy",
        body = "ExamAI is used by students of many ages, including some under 13. We do not " +
                "knowingly collect personal information from anyone, and the app itself " +
                "requires no account, login, or personal details from any user, child or " +
                "adult.\n\n" +
                "Because the app now serves ads via Google AdMob, we configure ad requests to " +
                "comply with COPPA and the Google Play Families Policy, and default to " +
                "non-personalized ads until consent is given. If you are a parent with " +
                "concerns about ads shown to a child using this app, please contact us."
    ),
    PolicySection(
        title = "Data Security",
        body = "Since ExamAI does not collect or transmit any personal data, there is no " +
                "personal data at risk. All data stored locally on your device is protected " +
                "by your device's built-in security (screen lock, encryption, etc.).\n\n" +
                "We recommend keeping your Android device updated for best security practices."
    ),
    PolicySection(
        title = "Data Retention",
        body = "We do not retain any user data on our servers because we do not have any " +
                "servers that collect user data.\n\n" +
                "Local data on your device (quiz scores, cached questions) is retained until you:\n" +
                "• Clear the app's cache in device settings\n" +
                "• Uninstall the app"
    ),
    PolicySection(
        title = "Your Rights",
        body = "Even though we collect no personal data, you have the following rights " +
                "regarding your local app data:\n" +
                "• Right to Delete: Uninstall the app to delete all local data\n" +
                "• Right to Access: All your data is visible within the app itself\n" +
                "• Right to Portability: Your data stays on your own device"
    ),
    PolicySection(
        title = "Permissions We Request",
        body = "ExamAI requests only the following Android permissions:\n" +
                "• INTERNET — fetch live questions, study notes, and ads\n" +
                "• ACCESS_NETWORK_STATE — check if internet is available before fetching content\n" +
                "• POST_NOTIFICATIONS (Android 13+, optional) — show an optional daily practice " +
                "reminder, which you can decline or turn off anytime in Settings\n\n" +
                "We do not request permissions for Camera, Microphone, Location, Contacts, " +
                "Storage, Phone, SMS, or any other sensitive permission."
    ),
    PolicySection(
        title = "Third Party Links",
        body = "The app may display links to external websites such as Wikipedia or NCERT. " +
                "Once you leave the app and visit these websites, their own privacy policies " +
                "apply. We are not responsible for the privacy practices of these external sites."
    ),
    PolicySection(
        title = "Changes to This Privacy Policy",
        body = "We may update this Privacy Policy from time to time. When we do:\n" +
                "• The \"Last Updated\" date at the top will be changed\n" +
                "• Significant changes will be notified via a notice in the app\n" +
                "• Continued use of the app after changes means you accept the updated policy\n\n" +
                "We encourage you to review this policy periodically."
    ),
    PolicySection(
        title = "Compliance",
        body = "This app and privacy policy comply with:\n" +
                "✅ Google Play Developer Policy\n" +
                "✅ GDPR (General Data Protection Regulation) — EU\n" +
                "✅ COPPA (Children's Online Privacy Protection Act) — US\n" +
                "✅ IT Act 2000 & DPDP Act 2023 — India\n" +
                "✅ Wikipedia Terms of Use\n" +
                "✅ Open Trivia DB Terms of Use\n" +
                "✅ Google Firebase Terms of Service"
    ),
    PolicySection(
        title = "Contact Us",
        body = "If you have any questions, concerns, or requests regarding this Privacy " +
                "Policy, please contact us:\n\n" +
                "Developer: Raiyan Shahid\n" +
                "Email: raiyannshahid@gmail.com\n" +
                "Response Time: Within 48 hours"
    ),
    PolicySection(
        title = "Consent",
        body = "By downloading and using ExamAI – Smart Study Assistant, you confirm that " +
                "you have read and understood this Privacy Policy and agree to its terms."
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { BackTopBar(title = "Privacy Policy", onBack = onBack) }
    ) { padding ->
        PrivacyPolicyBody(modifier = Modifier.fillMaxSize().padding(padding))
    }
}

@Composable
internal fun PrivacyPolicyBody(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Last Updated: August 9, 2026 • Effective Date: June 22, 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(policySections) { section ->
            PolicySectionCard(section)
        }
    }
}

@Composable
private fun PolicySectionCard(section: PolicySection) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
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
                color = Color(0xFF424242),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PrivacyPolicyScreenPreview() {
    AIStudyAssistantTheme {
        PrivacyPolicyScreen(onBack = {})
    }
}
