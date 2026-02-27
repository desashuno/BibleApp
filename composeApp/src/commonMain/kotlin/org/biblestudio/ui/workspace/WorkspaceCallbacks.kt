package org.biblestudio.ui.workspace

/**
 * Bundles all workspace-level callbacks to keep the composable
 * parameter list within detekt's threshold.
 */
data class WorkspaceCallbacks(
    val onPaneSelected: (String) -> Unit = {},
    val onPaneClose: (String) -> Unit = {},
    val onSplitHorizontal: (String) -> Unit = {},
    val onSplitVertical: (String) -> Unit = {},
    val onResizeSplit: (List<Int>, Float) -> Unit = { _, _ -> },
    val onSwitchTab: (List<Int>, Int) -> Unit = { _, _ -> },
    val onSettingsClick: () -> Unit = {}
)
