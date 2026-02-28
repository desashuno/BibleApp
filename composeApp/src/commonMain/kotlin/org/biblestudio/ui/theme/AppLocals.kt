package org.biblestudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.biblestudio.features.settings.component.SettingsState

/**
 * Provides the user-configured font size (in sp) to all descendant composables.
 * Defaults to [SettingsState.DEFAULT_FONT_SIZE] (16).
 */
val LocalAppFontSize = compositionLocalOf { SettingsState.DEFAULT_FONT_SIZE }

/** Whether to display superscript verse numbers in the Bible reader. */
val LocalShowVerseNumbers = compositionLocalOf { true }

/** Whether to render Words of Jesus in red. */
val LocalRedLetter = compositionLocalOf { false }

/** Whether to display Bible text as flowing paragraphs instead of verse-per-line. */
val LocalParagraphMode = compositionLocalOf { false }

/** Whether to show all verses in the current book with chapter dividers (continuous scroll). */
val LocalContinuousScroll = compositionLocalOf { false }

/**
 * Returns a [TextStyle] based on `bodyLarge` but scaled to the
 * user's preferred font size from [LocalAppFontSize].
 *
 * Use this for primary reading content (Bible text, commentary,
 * notes, sermon sections) so the Settings font-size slider
 * takes effect globally.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun scaledBodyStyle(): TextStyle {
    val fontSize = LocalAppFontSize.current
    return MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp)
}
