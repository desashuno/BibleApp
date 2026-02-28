package org.biblestudio.features.workspace.domain.model

import org.biblestudio.core.error.AppError

/**
 * Observable state for the workspace, exposed by [WorkspaceComponent].
 *
 * Components update this via `StateFlow` — they never throw exceptions.
 */
data class WorkspaceState(
    val layout: LayoutNode? = LayoutNode.Leaf(paneType = "dashboard"),
    val workspaceName: String = "Default",
    val loading: Boolean = false,
    val error: AppError? = null
)
