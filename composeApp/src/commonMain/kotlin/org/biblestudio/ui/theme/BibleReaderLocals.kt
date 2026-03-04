package org.biblestudio.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import org.biblestudio.features.settings.component.SettingsState

/**
 * Aggregated Bible reader display settings.
 *
 * Individual [CompositionLocal]s ([LocalAppFontSize], [LocalShowVerseNumbers], etc.)
 * remain the canonical read API. This wrapper simplifies provision at the call site.
 */
@Stable
data class BibleReaderSettings(
    val fontSize: Int = SettingsState.DEFAULT_FONT_SIZE,
    val showVerseNumbers: Boolean = true,
    val redLetter: Boolean = false,
    val paragraphMode: Boolean = false,
    val continuousScroll: Boolean = false,
)

/**
 * Provides all five Bible-reader CompositionLocals in a single call.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProvideBibleReaderSettings(
    settings: BibleReaderSettings,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppFontSize provides settings.fontSize,
        LocalShowVerseNumbers provides settings.showVerseNumbers,
        LocalRedLetter provides settings.redLetter,
        LocalParagraphMode provides settings.paragraphMode,
        LocalContinuousScroll provides settings.continuousScroll,
        content = content,
    )
}
