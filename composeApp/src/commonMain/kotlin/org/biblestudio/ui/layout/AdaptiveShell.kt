package org.biblestudio.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Top-level adaptive container that chooses the correct shell
 * (mobile / tablet / desktop) based on the available width.
 *
 * The chosen [WindowSizeClass] is forwarded to [content] so
 * downstream composables can adapt independently.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun AdaptiveShell(modifier: Modifier = Modifier, content: @Composable (WindowSizeClass) -> Unit) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sizeClass = WindowSizeClass.fromWidth(maxWidth)
        content(sizeClass)
    }
}
