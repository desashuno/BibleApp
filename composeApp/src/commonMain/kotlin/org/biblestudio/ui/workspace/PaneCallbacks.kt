package org.biblestudio.ui.workspace

/** Callback bundle forwarded through the layout tree. */
data class PaneCallbacks(
    val onClose: (String) -> Unit = {},
    val onSplitHorizontal: (String) -> Unit = {},
    val onSplitVertical: (String) -> Unit = {},
    val onResizeSplit: (List<Int>, Float) -> Unit = { _, _ -> },
    val onSwitchTab: (List<Int>, Int) -> Unit = { _, _ -> }
)
