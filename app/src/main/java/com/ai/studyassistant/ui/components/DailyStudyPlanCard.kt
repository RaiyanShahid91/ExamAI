package com.ai.studyassistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.local.StudyPlanSettings
import com.ai.studyassistant.data.model.DailyStudyPlan
import com.ai.studyassistant.data.model.ExamData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.compose.ui.res.pluralStringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStudyPlanCard(
    plan: DailyStudyPlan,
    onSave: (StudyPlanSettings) -> Unit,
    onPractice: (String, String, String) -> Unit,
    onReview: () -> Unit
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.daily_study_plan_your_daily_focus), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { editing = true }) { Text(stringResource(R.string.daily_study_plan_edit)) }
            }
            Text(stringResource(R.string.daily_study_plan_daily_goal, plan.settings.exam), style = MaterialTheme.typography.labelLarge)
            plan.daysLeft?.let { days ->
                Text(countdownText(plan.settings.exam, days), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            plan.suggestedGoal?.let { suggested ->
                if (suggested != plan.settings.questionGoal) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(stringResource(R.string.daily_study_plan_to_reach_practice_questions_by, com.ai.studyassistant.data.model.ExamPacing.PRACTICE_TARGET, suggested),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onSave(plan.settings.copy(questionGoal = suggested)) }) { Text(stringResource(R.string.daily_study_plan_use, suggested)) }
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (plan.answered.toFloat() / plan.settings.questionGoal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.daily_study_plan_questions_answered, plan.answered, plan.settings.questionGoal))
            Text(stringResource(R.string.daily_study_plan_completed_quizzes_count_automat), style = MaterialTheme.typography.bodySmall)
            Text(if (plan.topicCompleted) stringResource(R.string.plan_practice_completed, plan.topic) else stringResource(R.string.plan_practice, plan.topic), fontWeight = FontWeight.SemiBold)
            Text(plan.subject, style = MaterialTheme.typography.bodySmall)
            if (!plan.topicCompleted || plan.answered < plan.settings.questionGoal) {
                Button(onClick = { onPractice(plan.settings.exam, plan.subject, plan.topic) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.daily_study_plan_practice_topic)) }
            }
            HorizontalDivider()
            Text(if (plan.dueReviews == 0) stringResource(R.string.plan_no_reviews_due, plan.settings.exam) else pluralStringResource(R.plurals.plan_reviews_due, plan.dueReviews, plan.dueReviews, plan.settings.exam))
            if (plan.reviewedToday > 0) Text(stringResource(R.string.daily_study_plan_questions_reviewed_today, plan.reviewedToday), style = MaterialTheme.typography.bodySmall)
            if (plan.dueReviews > 0) {
                OutlinedButton(onClick = onReview) { Text(stringResource(R.string.daily_study_plan_review_questions)) }
                Text(stringResource(R.string.daily_study_plan_review_sessions_include_all_you), style = MaterialTheme.typography.bodySmall)
            }
            if (plan.topicCompleted && plan.answered >= plan.settings.questionGoal && plan.dueReviews == 0) {
                Text(stringResource(R.string.daily_study_plan_today_s_plan_is_complete), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (editing) {
        var exam by rememberSaveable { mutableStateOf(plan.settings.exam) }
        var goal by rememberSaveable { mutableIntStateOf(plan.settings.questionGoal) }
        var examDate by rememberSaveable { mutableStateOf(plan.settings.examDateEpochDay) }
        var pickingDate by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(stringResource(R.string.daily_study_plan_your_daily_study_plan)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.daily_study_plan_choose_your_exam))
                    ExamData.exams.forEach { item ->
                        FilterChip(selected = exam == item.name, onClick = { exam = item.name }, label = { Text(item.name) })
                    }
                    Text(stringResource(R.string.daily_study_plan_daily_question_goal, goal))
                    Slider(value = goal.toFloat(), onValueChange = { goal = (it / 5).toInt() * 5 }, valueRange = 5f..50f, steps = 8)
                    Text(stringResource(R.string.daily_study_plan_choose_5_50_questions_progress))
                    HorizontalDivider()
                    Text(stringResource(R.string.daily_study_plan_exam_date))
                    Text(
                        examDate?.let { formatExamDate(it) } ?: stringResource(R.string.plan_exam_date_not_set),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickingDate = true }) { Text(if (examDate == null) stringResource(R.string.plan_set_date) else stringResource(R.string.plan_change_date)) }
                        if (examDate != null) TextButton(onClick = { examDate = null }) { Text(stringResource(R.string.daily_study_plan_clear)) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onSave(StudyPlanSettings(exam, goal, examDate)); editing = false }) { Text(stringResource(R.string.daily_study_plan_save)) } },
            dismissButton = { TextButton(onClick = { editing = false }) { Text(stringResource(R.string.reminders_cancel)) } }
        )

        if (pickingDate) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = (examDate ?: LocalDate.now().plusDays(30).toEpochDay()) * DAY_MILLIS
            )
            DatePickerDialog(
                onDismissRequest = { pickingDate = false },
                confirmButton = {
                    TextButton(onClick = {
                        // The picker reports midnight UTC of the chosen calendar day, i.e. an exact epoch day.
                        pickerState.selectedDateMillis?.let { examDate = Math.floorDiv(it, DAY_MILLIS) }
                        pickingDate = false
                    }) { Text(stringResource(R.string.daily_study_plan_ok)) }
                },
                dismissButton = { TextButton(onClick = { pickingDate = false }) { Text(stringResource(R.string.reminders_cancel)) } }
            ) { DatePicker(state = pickerState) }
        }
    }
}

private const val DAY_MILLIS = 86_400_000L

private fun formatExamDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

@Composable
private fun countdownText(exam: String, daysLeft: Int): String = when {
    daysLeft > 1 -> stringResource(R.string.plan_days_to_exam, daysLeft, exam)
    daysLeft == 1 -> stringResource(R.string.plan_one_day_to_exam, exam)
    daysLeft == 0 -> stringResource(R.string.plan_exam_today, exam)
    else -> stringResource(R.string.plan_exam_passed, exam)
}
