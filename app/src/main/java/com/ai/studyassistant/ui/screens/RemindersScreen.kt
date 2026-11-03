package com.ai.studyassistant.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.ai.studyassistant.data.local.PersonalReminderEntity
import com.ai.studyassistant.notification.NotificationHelper
import com.ai.studyassistant.notification.ReminderScheduler
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.ScreenIntro
import com.ai.studyassistant.viewmodel.RemindersViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun RemindersScreen(viewModel: RemindersViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsState()
    val dailyEnabled by viewModel.dailyEnabled.collectAsState()
    val error by viewModel.error.collectAsState()
    val saving by viewModel.saving.collectAsState()
    var editorVisible by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var permitted by remember { mutableStateOf(NotificationHelper.canNotify(context)) }
    var precise by remember { mutableStateOf(ReminderScheduler.preciseTimingAllowed(context)) }
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permitted = NotificationHelper.canNotify(context)
                precise = ReminderScheduler.preciseTimingAllowed(context)
                scope.launch { ReminderScheduler.restore(context) }
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permitted = NotificationHelper.canNotify(context)
        if (permitted) scope.launch { ReminderScheduler.restore(context) }
    }
    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
    }
    Scaffold(
        topBar = { BackTopBar(stringResource(R.string.reminders_title), onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(modifier = Modifier.testTag("add_reminder"), onClick = { editingId = null; viewModel.clearError(); editorVisible = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text(stringResource(R.string.reminders_add_reminder)) })
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ScreenIntro(stringResource(R.string.reminders_little_nudge), stringResource(R.string.reminders_make_room)) }
            if (!permitted) item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.reminders_allow_notifications_to_receive_reminde), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.reminders_your_reminders_stay_saved_here))
                        Button(onClick = { requestNotifications() }) { Text(stringResource(R.string.reminders_enable_notifications)) }
                        if (Build.VERSION.SDK_INT >= 33) TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text(stringResource(R.string.reminders_open_notification_settings)) }
                    }
                }
            }
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.reminders_daily_study_reminders), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Switch(checked = dailyEnabled, onCheckedChange = { enabled -> viewModel.setDailyEnabled(enabled); if (enabled && !permitted) requestNotifications() })
                        }
                        Text(stringResource(R.string.reminders_optional_up_to_2_a))
                        Text(stringResource(R.string.reminders_turn_this_on_to_receive), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (precise) stringResource(R.string.reminders_precise_enabled) else stringResource(R.string.reminders_about_timing), style = MaterialTheme.typography.titleMedium)
                        Text(if (precise) stringResource(R.string.reminders_precise_body) else stringResource(R.string.reminders_imprecise_body), style = MaterialTheme.typography.bodyMedium)
                        if (!precise && Build.VERSION.SDK_INT >= 31) OutlinedButton(onClick = {
                            try { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }
                            catch (_: android.content.ActivityNotFoundException) { }
                        }) { Text(stringResource(R.string.reminders_allow_precise_timing)) }
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text(stringResource(R.string.reminders_notification_settings)) }
                    }
                }
            }
            item { Text(stringResource(R.string.reminders_your_personal_reminders), style = MaterialTheme.typography.titleLarge) }
            error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            if (reminders.isEmpty()) item {
                Text(stringResource(R.string.reminders_nothing_to_remember_yet_add), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(reminders, key = { it.id }) { item ->
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        if (item.note.isNotBlank()) Text(item.note, style = MaterialTheme.typography.bodyMedium)
                        Text(Instant.ofEpochMilli(item.scheduledAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)), style = MaterialTheme.typography.bodySmall)
                        Text(if (item.delivered) stringResource(R.string.reminders_status_sent) else if (item.scheduledAt <= System.currentTimeMillis()) stringResource(R.string.reminders_status_due) else stringResource(R.string.reminders_status_upcoming), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        Row {
                            TextButton(modifier = Modifier.testTag("reminder_edit_${item.id}"), onClick = { editingId = item.id; viewModel.clearError(); editorVisible = true }) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)); Text(if (item.delivered) stringResource(R.string.reminders_schedule_again) else stringResource(R.string.reminders_edit), modifier = Modifier.padding(start = 6.dp)) }
                            TextButton(modifier = Modifier.testTag("reminder_delete_${item.id}"), onClick = { viewModel.delete(item.id) }) { Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp)); Text(stringResource(R.string.reminders_delete)) }
                        }
                    }
                }
            }
        }
    }
    if (editorVisible) {
        ReminderEditor(reminders.firstOrNull { it.id == editingId }, saving, error, onDismiss = { if (!saving) { editorVisible = false; viewModel.clearError() } }) { title, note, time ->
            viewModel.save(editingId, title, note, time) {
                editorVisible = false
                if (!permitted) requestNotifications()
            }
        }
    }
}

@Composable
private fun ReminderEditor(item: PersonalReminderEntity?, saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    val context = LocalContext.current
    val initial = remember(item?.id) {
        item?.scheduledAt?.takeIf { it > System.currentTimeMillis() }?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
            ?: LocalDateTime.now().plusHours(1).withSecond(0).withNano(0)
    }
    var title by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var note by rememberSaveable(item?.id) { mutableStateOf(item?.note.orEmpty()) }
    var date by rememberSaveable(item?.id) { mutableStateOf(initial.toLocalDate().toString()) }
    var hour by rememberSaveable(item?.id) { mutableIntStateOf(initial.hour) }
    var minute by rememberSaveable(item?.id) { mutableIntStateOf(initial.minute) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) stringResource(R.string.reminders_new) else stringResource(R.string.reminders_edit_title)) },
        text = {
            Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it.take(100) }, label = { Text(stringResource(R.string.reminders_what_would_you_like_to)) }, placeholder = { Text(stringResource(R.string.reminders_revise_organic_chemistry)) }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("reminder_title"))
                OutlinedTextField(value = note, onValueChange = { note = it.take(500) }, label = { Text(stringResource(R.string.reminders_a_note_for_yourself_optional)) }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth().testTag("reminder_note"))
                OutlinedButton(onClick = {
                    val selected = LocalDate.parse(date)
                    DatePickerDialog(context, { _, year, month, day -> date = LocalDate.of(year, month + 1, day).toString() }, selected.year, selected.monthValue - 1, selected.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) { Text(LocalDate.parse(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))) }
                OutlinedButton(onClick = {
                    TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, android.text.format.DateFormat.is24HourFormat(context)).show()
                }, modifier = Modifier.fillMaxWidth()) { Text(LocalTime.of(hour, minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))) }
                Text(stringResource(R.string.reminders_sends_one_notification_at_your), style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = !saving, onClick = {
            onSave(title, note, LocalDate.parse(date).atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }) { Text(if (saving) stringResource(R.string.reminders_saving) else stringResource(R.string.reminders_save)) } },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text(stringResource(R.string.reminders_cancel)) } }
    )
}
