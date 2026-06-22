package com.ai.studyassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ai.studyassistant.navigation.NavGraph
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIStudyAssistantTheme {
                ExamAIApp()
            }
        }
    }
}

@Composable
private fun ExamAIApp() {
    val navController = rememberNavController()
    val viewModel: QuizViewModel = viewModel()

    NavGraph(
        navController = navController,
        viewModel = viewModel
    )
}
