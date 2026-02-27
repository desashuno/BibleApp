package org.biblestudio.features.exegetical_guide.data.mappers

import migrations.Resource_entries
import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry

internal fun Resource_entries.toCommentaryEntry(): CommentaryEntry = CommentaryEntry(
    id = id,
    resourceId = resource_id,
    globalVerseId = global_verse_id,
    content = content,
    sortOrder = sort_order
)
