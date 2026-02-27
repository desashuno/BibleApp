package org.biblestudio.features.resource_library.data.mappers

import migrations.Resource_entries
import migrations.Resources
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry

// ── Resource ────────────────────────────────────────────────────────

internal fun Resources.toResource(): Resource = Resource(
    uuid = uuid,
    type = type,
    title = title,
    author = author,
    version = version,
    format = format,
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)

// ── ResourceEntry ───────────────────────────────────────────────────

internal fun Resource_entries.toResourceEntry(): ResourceEntry = ResourceEntry(
    id = id,
    resourceId = resource_id,
    globalVerseId = global_verse_id,
    content = content,
    sortOrder = sort_order
)
