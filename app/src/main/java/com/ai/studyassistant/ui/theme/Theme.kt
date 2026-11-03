package com.ai.studyassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.annotation.StringRes
import com.ai.studyassistant.R

enum class ThemeMode(@StringRes val labelRes: Int) { SYSTEM(R.string.theme_system), LIGHT(R.string.theme_light), DARK(R.string.theme_dark) }

enum class AppPalette(@StringRes val labelRes: Int, @StringRes val captionRes: Int, val seed: Color, val bright: Color) {
    OCEAN(R.string.palette_ocean, R.string.palette_ocean_caption, Color(0xFF245BC6), Color(0xFFAFC6FF)),
    VIOLET(R.string.palette_violet, R.string.palette_violet_caption, Color(0xFF7046B8), Color(0xFFD3BBFF)),
    FOREST(R.string.palette_forest, R.string.palette_forest_caption, Color(0xFF24694F), Color(0xFF93D6B4)),
    ROSE(R.string.palette_rose, R.string.palette_rose_caption, Color(0xFFA63760), Color(0xFFFFB0CB)),
    AMBER(R.string.palette_amber, R.string.palette_amber_caption, Color(0xFF885316), Color(0xFFFFCB8F))
}

val LocalDarkAppearance = staticCompositionLocalOf { false }

fun appColorScheme(palette: AppPalette, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = palette.bright, onPrimary = lerp(palette.seed, Color.Black, 0.65f),
        primaryContainer = lerp(palette.seed, Color(0xFF161A23), 0.58f), onPrimaryContainer = palette.bright,
        secondary = Color(0xFFB6C7BC), onSecondary = Color(0xFF20352B),
        secondaryContainer = Color(0xFF293D35), onSecondaryContainer = Color(0xFFD3EBDD),
        tertiary = Color(0xFFEBC493), onTertiary = Color(0xFF432C12),
        tertiaryContainer = Color(0xFF493923), onTertiaryContainer = Color(0xFFFFDEB6),
        background = lerp(Color(0xFF11141B), palette.seed, 0.035f),
        onBackground = Color(0xFFE8EAF0), surface = Color(0xFF1B1F29), onSurface = Color(0xFFE8EAF0),
        surfaceVariant = Color(0xFF282E3B), onSurfaceVariant = Color(0xFFBCC2D1),
        surfaceContainer = Color(0xFF202531), surfaceContainerLow = Color(0xFF171C25),
        surfaceContainerHigh = Color(0xFF292F3C), surfaceContainerHighest = Color(0xFF323948),
        outline = Color(0xFF8992A4), outlineVariant = Color(0xFF363E4E),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF6B2026), onErrorContainer = Color(0xFFFFDAD6)
    )
} else {
    lightColorScheme(
        primary = palette.seed, onPrimary = Color.White,
        primaryContainer = lerp(Color.White, palette.seed, 0.12f), onPrimaryContainer = lerp(palette.seed, Color.Black, 0.35f),
        secondary = Color(0xFF406451), onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0EEE5), onSecondaryContainer = Color(0xFF203B2C),
        tertiary = Color(0xFF805A29), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFEBD2), onTertiaryContainer = Color(0xFF4D3211),
        background = lerp(Color(0xFFF8F9FC), palette.seed, 0.018f), onBackground = Color(0xFF202634),
        surface = Color.White, onSurface = Color(0xFF202634),
        surfaceVariant = Color(0xFFEDF0F6), onSurfaceVariant = Color(0xFF596477),
        surfaceContainer = Color(0xFFF0F2F8), surfaceContainerLow = Color(0xFFF6F7FB),
        surfaceContainerHigh = Color(0xFFE9EDF5), surfaceContainerHighest = Color(0xFFE2E7F0),
        outline = Color(0xFF737E91), outlineVariant = Color(0xFFDDE2EC),
        error = Color(0xFFB32632), onError = Color.White,
        errorContainer = Color(0xFFFFE2E4), onErrorContainer = Color(0xFF7D1523)
    )
}

object GradientTokens {
    @Composable
    fun hero(darkTheme: Boolean = LocalDarkAppearance.current): List<Color> {
        val colors = MaterialTheme.colorScheme
        return listOf(colors.background, colors.background, colors.primaryContainer.copy(alpha = if (darkTheme) 0.65f else 0.4f))
    }
}

@Composable
fun AIStudyAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.OCEAN,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDarkAppearance provides darkTheme) {
        MaterialTheme(colorScheme = appColorScheme(palette, darkTheme), typography = Typography, shapes = AppShapes, content = content)
    }
}
