package org.biblestudio.features.cross_references.data.mappers

import migrations.Cross_references
import migrations.Parallel_passages
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.cross_references.domain.entities.ParallelPassage

// ── CrossReference ──────────────────────────────────────────────────

internal fun Cross_references.toCrossReference(): CrossReference = CrossReference(
    id = id,
    sourceVerseId = source_verse_id,
    targetVerseId = target_verse_id,
    type = type,
    confidence = confidence
)

// ── ParallelPassage ─────────────────────────────────────────────────

internal fun Parallel_passages.toParallelPassage(): ParallelPassage = ParallelPassage(
    id = id,
    groupId = group_id,
    globalVerseId = global_verse_id,
    label = label
)
