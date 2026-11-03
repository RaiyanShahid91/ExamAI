package com.ai.studyassistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ai.studyassistant.ui.theme.AppPalette
import com.ai.studyassistant.ui.theme.ThemeMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import androidx.datastore.preferences.core.emptyPreferences

private val Context.appearanceStore by preferencesDataStore(name = "appearance")

data class AppearanceSettings(
    val palette: AppPalette = AppPalette.OCEAN,
    val mode: ThemeMode = ThemeMode.SYSTEM
)

class AppearancePreferences(private val context: Context) {
    private val paletteKey = stringPreferencesKey("palette")
    private val modeKey = stringPreferencesKey("mode")
    val settings = context.appearanceStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { values ->
        AppearanceSettings(
            AppPalette.entries.firstOrNull { it.name == values[paletteKey] } ?: AppPalette.OCEAN,
            ThemeMode.entries.firstOrNull { it.name == values[modeKey] } ?: ThemeMode.SYSTEM
        )
    }
    suspend fun save(settings: AppearanceSettings) {
        context.appearanceStore.edit {
            it[paletteKey] = settings.palette.name
            it[modeKey] = settings.mode.name
        }
    }
}
