package org.biblestudio.core.pane_registry

/**
 * Describes a registered pane type for use in the activity bar, "Add Pane"
 * picker, and workspace presets.
 *
 * @param type Unique key used to look up the pane (e.g. `"bible-reader"`).
 * @param displayName Human-readable label shown in the UI.
 * @param icon Material icon name for the activity bar / picker.
 * @param category Logical grouping for filtering and theming.
 * @param description Short description shown in tooltips and the picker.
 */
data class PaneMetadata(
    val type: String,
    val displayName: String,
    val icon: String,
    val category: PaneCategory,
    val description: String
)
