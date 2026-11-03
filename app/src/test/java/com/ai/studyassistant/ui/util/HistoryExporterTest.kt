package com.ai.studyassistant.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryExporterTest {

    @Test fun csvField_quotesAndEscapes() {
        assertEquals("\"Newton's \"\"Laws\"\"\"", HistoryExporter.csvField("Newton's \"Laws\""))
    }

    @Test fun csvField_neutralisesFormulaPrefixes() {
        assertEquals("\"'=SUM(A1)\"", HistoryExporter.csvField("=SUM(A1)"))
        assertEquals("\"'@cmd\"", HistoryExporter.csvField("@cmd"))
    }

    @Test fun csvField_keepsCommasInsideQuotes() {
        assertEquals("\"a,b\"", HistoryExporter.csvField("a,b"))
    }
}
