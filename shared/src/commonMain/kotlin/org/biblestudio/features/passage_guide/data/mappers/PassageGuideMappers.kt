package org.biblestudio.features.passage_guide.data.mappers

import migrations.Outlines
import org.biblestudio.features.passage_guide.domain.entities.Outline

internal fun Outlines.toOutline(): Outline = Outline(
    id = id,
    globalVerseStart = global_verse_start,
    globalVerseEnd = global_verse_end,
    title = title,
    content = content,
    source = source
)
