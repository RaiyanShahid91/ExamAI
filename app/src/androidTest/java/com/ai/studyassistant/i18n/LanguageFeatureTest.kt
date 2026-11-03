package com.ai.studyassistant.i18n

import android.content.res.Configuration
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ai.studyassistant.MainActivity
import com.ai.studyassistant.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LanguageFeatureTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun shippedResourcesFormatForEveryLanguageAndPluralCount() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (language in LanguageManager.supported) {
            val config = Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(language.tag))
            }
            val localized = context.createConfigurationContext(config)
            val title = localized.getString(R.string.language_title)
            assertTrue(title.isNotBlank())
            if (language.tag != "en") assertNotEquals("Language", title)
            assertEquals(if (language.tag == "ur") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR, config.layoutDirection)
            for (count in listOf(0, 1, 2, 5, 15, 100)) {
                for (id in listOf(R.plurals.home_streak_days, R.plurals.widget_reviews_due, R.plurals.notes_searches_left, R.plurals.notif_due_body, R.plurals.flashcards_need_another_look)) {
                    assertFalse(localized.resources.getQuantityString(id, count, count).contains("%1\$d"))
                }
            }
            assertTrue(localized.getString(R.string.quiz_question_of, 2, 10).isNotBlank())
            assertTrue(localized.getString(R.string.plan_days_to_exam, 40, "JEE").contains("JEE"))
        }
    }

    @Test fun pickerSwitchesHindiTamilUrduAndPersistsThroughRecreation() {
        var previous: String? = null
        ui.activityRule.scenario.onActivity { previous = LanguageManager.currentTag(it) }
        try {
            for (tag in listOf("hi", "ta", "ur", "en")) {
                openLanguagePicker()
                ui.onNode(hasText(LanguageManager.nativeNameFor(tag)!!) and hasAnyAncestor(isDialog())).performScrollTo().performClick()
                ui.waitUntil(15_000) {
                    var current: String? = null
                    ui.activityRule.scenario.onActivity { current = LanguageManager.currentTag(it) }
                    current == tag
                }
                ui.waitForIdle()
                ui.activityRule.scenario.onActivity {
                    assertEquals(tag, it.resources.configuration.locales[0].language)
                    assertEquals(if (tag == "ur") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR, it.resources.configuration.layoutDirection)
                }
                val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                java.io.File(ui.activity.cacheDir, "language-$tag.png").outputStream().use {
                    screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                screenshot.recycle()
                ui.activityRule.scenario.recreate()
                ui.activityRule.scenario.onActivity { assertEquals(tag, LanguageManager.currentTag(it)) }
                ui.waitForIdle()
            }
            // Returning to system default must also restore the phone's resource locale.
            ui.activityRule.scenario.onActivity { LanguageManager.apply(it, null) }
            ui.waitUntil(15_000) {
                var system = false
                ui.activityRule.scenario.onActivity { system = LanguageManager.currentTag(it) == null }
                system
            }
        } finally {
            ui.activityRule.scenario.onActivity { LanguageManager.apply(it, previous) }
        }
    }

    private fun openLanguagePicker() {
        ui.waitUntil(15_000) {
            runCatching {
                val c = ui.activity
                ui.onAllNodesWithContentDescription(c.getString(R.string.language_title)).fetchSemanticsNodes().isNotEmpty() ||
                    ui.onAllNodesWithContentDescription(c.getString(R.string.home_settings_appearance)).fetchSemanticsNodes().isNotEmpty() ||
                    ui.onAllNodesWithText(c.getString(R.string.settings_settings)).fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
        }
        val language = ui.activity.getString(R.string.language_title)
        val settings = ui.activity.getString(R.string.home_settings_appearance)
        // The picker is available both during first-run onboarding and in Settings.
        if (ui.onAllNodesWithContentDescription(language).fetchSemanticsNodes().isNotEmpty()) {
            ui.onNodeWithContentDescription(language).performClick()
        } else {
            if (ui.onAllNodesWithContentDescription(settings).fetchSemanticsNodes().isNotEmpty()) {
                ui.onNodeWithContentDescription(settings).performClick()
            }
            val label = ui.activity.getString(R.string.settings_language)
            ui.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(label))
            ui.onNodeWithText(label).performClick()
        }
        ui.onNode(hasText(LanguageManager.nativeNameFor("hi")!!) and hasAnyAncestor(isDialog())).assertExists()
    }
}
