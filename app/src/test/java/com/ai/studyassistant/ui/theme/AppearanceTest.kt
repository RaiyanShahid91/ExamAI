package com.ai.studyassistant.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceTest {
    private fun contrast(a: Color, b: Color): Float {
        val high = maxOf(a.luminance(), b.luminance())
        val low = minOf(a.luminance(), b.luminance())
        return (high + 0.05f) / (low + 0.05f)
    }

    @Test fun everyPaletteHasReadableTextInBothModes() {
        AppPalette.entries.forEach { palette ->
            listOf(false, true).forEach { dark ->
                val colors = appColorScheme(palette, dark)
                listOf(
                    colors.onBackground to colors.background,
                    colors.onSurface to colors.surface,
                    colors.onSurfaceVariant to colors.surface,
                    colors.onPrimary to colors.primary,
                    colors.onPrimaryContainer to colors.primaryContainer,
                    colors.primary to colors.background
                ).forEach { (foreground, background) ->
                    assertTrue("${palette.name}, dark=$dark: insufficient text contrast", contrast(foreground, background) >= 4.5f)
                }
            }
        }
    }
}
