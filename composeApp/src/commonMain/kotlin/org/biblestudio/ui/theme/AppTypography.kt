package org.biblestudio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Font family placeholders.
 *
 * Custom fonts (Merriweather, Source Sans 3, JetBrains Mono) will be loaded
 * from `composeResources/font/` once the font files are bundled.
 * Until then, serif / sans-serif / monospace defaults are used.
 */
private val MerriweatherFamily = FontFamily.Serif
private val SourceSans3Family = FontFamily.SansSerif

/** Reserved for Strong's numbers, morphology codes, and code snippets. */
@Suppress("unused")
val JetBrainsMonoFamily = FontFamily.Monospace

@Suppress("MagicNumber")
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (32 * 1.3).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = (28 * 1.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (24 * 1.35).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (20 * 1.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (18 * 1.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = (16 * 1.4).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MerriweatherFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (18 * 1.6).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (14 * 1.5).sp
    ),
    bodySmall = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = (12 * 1.5).sp
    ),
    labelLarge = TextStyle(
        fontFamily = SourceSans3Family,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = (14 * 1.4).sp
    )
)
