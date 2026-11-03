package com.ai.studyassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.local.AppearanceSettings
import com.ai.studyassistant.ui.theme.AppPalette
import com.ai.studyassistant.ui.theme.ThemeMode
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun AppearancePicker(settings: AppearanceSettings, onChange: (AppearanceSettings) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.appearance_make_it_feel_like_you), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.appearance_choose_your_colors_and_reading), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.mode == mode,
                    onClick = { onChange(settings.copy(mode = mode)) },
                    label = { Text(stringResource(mode.labelRes)) }, modifier = Modifier.weight(1f),
                    leadingIcon = if (settings.mode == mode) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null
                )
            }
        }
        AppPalette.entries.forEach { palette ->
            val chosen = settings.palette == palette
            Surface(
                onClick = { onChange(settings.copy(palette = palette)) },
                shape = MaterialTheme.shapes.medium,
                color = if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().semantics { selected = chosen; role = Role.RadioButton }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(40.dp).background(palette.seed, CircleShape), contentAlignment = Alignment.Center) {
                        if (chosen) Icon(Icons.Default.Check, stringResource(R.string.appearance_selected), tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(palette.labelRes), fontWeight = FontWeight.SemiBold)
                        Text(stringResource(palette.captionRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.size(20.dp).background(palette.bright, CircleShape))
                }
            }
        }
    }
}

@Composable
fun ScreenIntro(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
