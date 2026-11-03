package com.ai.studyassistant.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ai.studyassistant.R
import com.ai.studyassistant.navigation.NavGraph
import com.ai.studyassistant.navigation.Routes
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.QuizViewModel
import com.ai.studyassistant.viewmodel.UpdateViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFeatureTest {
    @get:Rule val ui = createComposeRule()

    @Test fun tourExplainsFeaturesOpensPolicyAndFinishesAtHome() {
        lateinit var nav: NavHostController
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        ui.setContent {
            nav = rememberNavController()
            val quiz: QuizViewModel = viewModel()
            val update: UpdateViewModel = viewModel()
            AIStudyAssistantTheme { NavGraph(nav, quiz, update, startDestination = Routes.ONBOARDING) }
        }
        val lastCards = listOf(R.string.onboarding_card_1c, R.string.onboarding_card_2c,
            R.string.onboarding_card_revision_c, R.string.onboarding_card_progress_c)
        for ((page, card) in lastCards.withIndex()) {
            ui.onNodeWithTag("onboarding_page_$page").assertIsDisplayed()
            snapshot("onboarding-$page")
            ui.onNodeWithText(app.getString(card)).performScrollTo().assertIsDisplayed()
            ui.onNodeWithText(app.getString(R.string.onboarding_continue)).performClick()
        }
        ui.onNodeWithText(app.getString(R.string.onboarding_language_options)).assertIsDisplayed()
        ui.onNodeWithText(app.getString(R.string.onboarding_content_language)).assertIsDisplayed()
        snapshot("onboarding-appearance")
        ui.onNodeWithTag("onboarding_privacy").performScrollTo().performClick()
        ui.waitUntil(5_000) { nav.currentDestination?.route == Routes.PRIVACY_POLICY }
        ui.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("2. Study data stored on your device"))
        ui.onNodeWithText("2. Study data stored on your device").assertIsDisplayed()
        snapshot("onboarding-privacy")
        ui.runOnIdle { nav.popBackStack() }
        ui.waitUntil(5_000) { nav.currentDestination?.route == Routes.ONBOARDING }
        ui.onNodeWithText(app.getString(R.string.onboarding_lets_start)).performClick()
        ui.waitUntil(5_000) { nav.currentDestination?.route == Routes.HOME }
        ui.onNodeWithContentDescription(app.getString(R.string.home_settings_appearance)).assertIsDisplayed()
    }

    private fun snapshot(name: String) {
        ui.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        java.io.File(context.filesDir, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
