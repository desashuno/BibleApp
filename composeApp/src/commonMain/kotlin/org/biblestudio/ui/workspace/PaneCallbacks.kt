package org.biblestudio.ui.workspace

import org.biblestudio.features.workspace.domain.model.PanePlacement

/** Callback bundle forwarded through the layout tree. */
data class PaneCallbacks(
    val onClose: (String) -> Unit = {},
    val onCloseAtPath: (List<Int>) -> Unit = {},
    val onSplitHorizontal: (String) -> Unit = {},
    val onSplitVertical: (String) -> Unit = {},
    val onResizeSplit: (List<Int>, Float) -> Unit = { _, _ -> },
    val onSwitchTab: (List<Int>, Int) -> Unit = { _, _ -> },
    val onReorderTab: (List<Int>, Int, Int) -> Unit = { _, _, _ -> },
    val onRearrangePane: (List<Int>, List<Int>, PanePlacement) -> Unit = { _, _, _ -> }
)
