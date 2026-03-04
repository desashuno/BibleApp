package org.biblestudio.core.navigation

/**
 * Determines how a target pane is opened when navigating from one pane
 * to another (e.g. via the Bible Reader context menu).
 */
enum class OpenMode {
    /** Reuse the target pane if it already exists; otherwise split to create it. */
    SMART,

    /** Always add a new pane in the current workspace. */
    NEW_PANEL,

    /** Create a new workspace with a Bible Reader and the target pane. */
    NEW_WORKSPACE
}
