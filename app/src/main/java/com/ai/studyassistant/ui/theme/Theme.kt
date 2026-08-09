package com.ai.studyassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandTeal,
    onPrimary = Color(0xFF00363A),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFCFF4F5),
    secondary = BrandOrange,
    onSecondary = Color(0xFF3A1400),
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = Color(0xFFCFFFFD),
    tertiary = BrandOrangeLight,
    onTertiary = Color(0xFF3A1400),
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = Color(0xFFFFDBCB),
    background = DarkBackground,
    onBackground = Color(0xFFE7EDF7),
    surface = DarkSurface,
    onSurface = Color(0xFFE7EDF7),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBECAE3),
    outline = DarkOutline,
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF2E0000),
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = Color(0xFF0B1D3D),
    secondary = BrandTealDark,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = Color(0xFF00302F),
    tertiary = BrandOrangeDark,
    onTertiary = Color.White,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = Color(0xFF411500),
    background = LightBackground,
    onBackground = Color(0xFF13223F),
    surface = LightSurface,
    onSurface = Color(0xFF13223F),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF44536E),
    outline = LightOutline,
    error = Color(0xFFBA1B1B),
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

/**
 * Hero-gradient stops matching the app logo: navy bookends around the bulb's
 * orange-to-teal glow, so screens don't hard-code brand colors.
 */
object GradientTokens {
    val lightHero = listOf(BrandNavy, BrandOrange, BrandTeal)
    val darkHero = listOf(BrandNavyDark, BrandOrangeDark, BrandTealDark)

    @Composable
    fun hero(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme) darkHero else lightHero
}

@Composable
fun AIStudyAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Status bar appearance is fixed once in MainActivity via enableEdgeToEdge's
    // SystemBarStyle — do not set it here too (it's a no-op on API 35+ anyway and
    // fighting over it per-recomposition is what caused the flicker).

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
