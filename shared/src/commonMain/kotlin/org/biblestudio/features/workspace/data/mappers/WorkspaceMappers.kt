package org.biblestudio.features.workspace.data.mappers

import migrations.Workspace_layouts
import migrations.Workspaces
import org.biblestudio.features.workspace.domain.entities.Workspace
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout

// ── Workspace ───────────────────────────────────────────────────────

internal fun Workspaces.toWorkspace(): Workspace = Workspace(
    uuid = uuid,
    name = name,
    isActive = is_active == 1L,
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)

// ── WorkspaceLayout ─────────────────────────────────────────────────

internal fun Workspace_layouts.toWorkspaceLayout(): WorkspaceLayout = WorkspaceLayout(
    id = id,
    workspaceId = workspace_id,
    layoutJson = layout_json,
    updatedAt = updated_at
)
