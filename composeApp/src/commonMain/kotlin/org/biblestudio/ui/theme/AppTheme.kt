package org.biblestudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5B4A3F),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFAF8F5),
    onSurface = Color(0xFF2C2520),
    surfaceVariant = Color(0xFFF0EDE8),
    error = Color(0xFFB3261E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC4A882),
    onPrimary = Color(0xFF1A1410),
    surface = Color(0xFF1E1B18),
    onSurface = Color(0xFFE8E0D8),
    surfaceVariant = Color(0xFF2A2520),
    error = Color(0xFFF2B8B5)
)

/**
 * Top-level BibleStudio theme.
 *
 * Wraps Material 3 [MaterialTheme] with the project's colour palette,
 * typography scale, and shape definitions.
 *
 * @param darkTheme Whether to use the dark colour scheme.
 *   Defaults to the system setting.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
