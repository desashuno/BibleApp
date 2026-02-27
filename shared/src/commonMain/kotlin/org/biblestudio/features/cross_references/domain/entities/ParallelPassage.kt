package org.biblestudio.features.cross_references.domain.entities

/**
 * Groups verses that are synoptic or thematic parallels.
 *
 * @param id Auto-generated database ID.
 * @param groupId Identifier linking all verses in the same parallel group.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param label Human-readable label (e.g., "Feeding of the 5000 — Matthew").
 */
data class ParallelPassage(
    val id: Long,
    val groupId: Long,
    val globalVerseId: Long,
    val label: String
)
