package org.biblestudio.features.sermon_editor.data.mappers

import migrations.Sermon_sections
import migrations.Sermons
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection

// ── Sermon ──────────────────────────────────────────────────────────

internal fun Sermons.toSermon(): Sermon = Sermon(
    uuid = uuid,
    title = title,
    scriptureRef = scripture_ref,
    createdAt = created_at,
    updatedAt = updated_at,
    status = status,
    deviceId = device_id
)

// ── SermonSection ───────────────────────────────────────────────────

internal fun Sermon_sections.toSermonSection(): SermonSection = SermonSection(
    id = id,
    sermonId = sermon_id,
    type = type,
    content = content,
    sortOrder = sort_order
)
