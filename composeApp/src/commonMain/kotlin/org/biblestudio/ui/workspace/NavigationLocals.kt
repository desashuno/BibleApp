package org.biblestudio.ui.workspace

import androidx.compose.runtime.compositionLocalOf
import org.biblestudio.core.navigation.OpenMode
import org.biblestudio.core.verse_bus.LinkEvent

/**
 * Provides a navigation callback that any pane can use to open or focus
 * another pane with a specific [OpenMode] and [LinkEvent].
 *
 * Provided by [WorkspaceShell] and wired through [WorkspaceCallbacks].
 */
val LocalNavigateToPane = compositionLocalOf<((String, OpenMode, LinkEvent) -> Unit)?> { null }
