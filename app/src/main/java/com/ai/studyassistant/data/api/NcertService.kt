package com.ai.studyassistant.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class NcertLink(val title: String, val url: String)

/**
 * Best-effort scraper for the public NCERT textbook index. NCERT serves chapters as PDFs
 * rather than structured HTML, so this only surfaces links to official PDFs/pages rather
 * than extracted chapter text. Any scraping failure (layout change, timeout, etc.) degrades
 * to an empty list rather than crashing the app — this is a supplementary feature, not a
 * dependency of the core quiz/notes flow.
 */
object NcertService {
    private const val TEXTBOOK_INDEX_URL = "https://ncert.nic.in/textbook.php"

    suspend fun fetchTextbookLinks(): List<NcertLink> = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(TEXTBOOK_INDEX_URL)
                .userAgent("Mozilla/5.0 (Android) ExamAI-StudyAssistant")
                .timeout(15_000)
                .get()

            document.select("a[href]")
                .mapNotNull { element ->
                    val href = element.attr("abs:href")
                    val text = element.text().trim()
                    if (text.isNotBlank() && href.contains("ncert", ignoreCase = true)) {
                        NcertLink(title = text, url = href)
                    } else {
                        null
                    }
                }
                .distinctBy { it.url }
                .take(30)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
