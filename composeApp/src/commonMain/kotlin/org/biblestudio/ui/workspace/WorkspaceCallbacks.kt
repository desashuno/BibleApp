package org.biblestudio.ui.workspace

import org.biblestudio.core.navigation.OpenMode
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.features.workspace.domain.model.PanePlacement

/**
 * Bundles all workspace-level callbacks to keep the composable
 * parameter list within detekt's threshold.
 */
data class WorkspaceCallbacks(
    val onPaneSelected: (String) -> Unit = {},
    val onPaneClose: (String) -> Unit = {},
    val onPaneCloseAtPath: (List<Int>) -> Unit = {},
    val onSplitHorizontal: (String) -> Unit = {},
    val onSplitVertical: (String) -> Unit = {},
    val onResizeSplit: (List<Int>, Float) -> Unit = { _, _ -> },
    val onSwitchTab: (List<Int>, Int) -> Unit = { _, _ -> },
    val onReorderTab: (List<Int>, Int, Int) -> Unit = { _, _, _ -> },
    val onRearrangePane: (List<Int>, List<Int>, PanePlacement) -> Unit = { _, _, _ -> },
    val onSettingsClick: () -> Unit = {},
    val onTogglePinned: (String) -> Unit = {},
    val onToggleFavorite: (String) -> Unit = {},
    val onLoadWorkspace: (String) -> Unit = {},
    val onCreateWorkspace: (String) -> Unit = {},
    val onDeleteWorkspace: (String) -> Unit = {},
    val onNavigateToPane: (String, OpenMode, LinkEvent) -> Unit = { _, _, _ -> }
)
