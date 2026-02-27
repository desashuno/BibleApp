package org.biblestudio.features.workspace.domain.entities

/**
 * Persisted layout state for a [Workspace].
 *
 * @param id Auto-generated database ID.
 * @param workspaceId FK to the parent [Workspace].
 * @param layoutJson JSON-serialized `LayoutNode` tree.
 * @param updatedAt ISO 8601 timestamp of the last layout save.
 */
data class WorkspaceLayout(
    val id: Long,
    val workspaceId: String,
    val layoutJson: String,
    val updatedAt: String
)
