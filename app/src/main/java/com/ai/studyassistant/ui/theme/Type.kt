package com.ai.studyassistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun style(size: Int, height: Int, weight: FontWeight = FontWeight.Normal, spacing: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = height.sp,
    fontWeight = weight, letterSpacing = spacing.sp
)
val Typography = Typography(
    displayLarge = style(52, 60, FontWeight.Bold, -1.5),
    displayMedium = style(44, 52, FontWeight.Bold, -1.0),
    displaySmall = style(36, 44, FontWeight.Bold, -0.8),
    headlineLarge = style(32, 40, FontWeight.Bold, -0.8),
    headlineMedium = style(28, 36, FontWeight.Bold, -0.6),
    headlineSmall = style(24, 32, FontWeight.Bold, -0.4),
    titleLarge = style(22, 30, FontWeight.SemiBold, -0.3),
    titleMedium = style(17, 25, FontWeight.SemiBold),
    titleSmall = style(15, 22, FontWeight.SemiBold),
    bodyLarge = style(16, 26), bodyMedium = style(14, 22), bodySmall = style(12, 19),
    labelLarge = style(14, 20, FontWeight.SemiBold),
    labelMedium = style(12, 18, FontWeight.Medium), labelSmall = style(11, 16, FontWeight.Medium)
)
