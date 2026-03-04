package org.biblestudio.ui.workspace

import androidx.compose.runtime.staticCompositionLocalOf
import org.biblestudio.features.worship.WorshipPlayer

/**
 * CompositionLocal providing the global [WorshipPlayer] singleton.
 * Provided at the App level so that any composable (mini-player, pane, etc.)
 * can access the shared playback state.
 */
val LocalWorshipPlayer = staticCompositionLocalOf<WorshipPlayer?> { null }
