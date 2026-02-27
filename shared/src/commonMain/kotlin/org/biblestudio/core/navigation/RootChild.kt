package org.biblestudio.core.navigation

/**
 * Sealed class representing the child instances produced by the root
 * navigation stack. Each variant wraps the component for that screen.
 *
 * For now the children are lightweight marker objects; full component
 * implementations will be added in later phases.
 */
sealed class RootChild {

    /** Workspace multi-pane layout child. */
    data object Workspace : RootChild()

    /** Settings screen child. */
    data object Settings : RootChild()

    /** Import wizard child. */
    data object Import : RootChild()
}
