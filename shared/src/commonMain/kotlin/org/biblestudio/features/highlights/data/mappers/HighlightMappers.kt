package org.biblestudio.features.highlights.data.mappers

import migrations.Highlights
import org.biblestudio.features.highlights.domain.entities.Highlight

internal fun Highlights.toHighlight(): Highlight = Highlight(
    uuid = uuid,
    globalVerseId = global_verse_id,
    colorIndex = color_index,
    style = style,
    startOffset = start_offset,
    endOffset = end_offset,
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)
