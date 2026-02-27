package org.biblestudio.features.cross_references.domain.entities

/**
 * A cross-reference link between two Bible verses.
 *
 * @param id Auto-generated database ID.
 * @param sourceVerseId The BBCCCVVV-encoded verse the reference originates from.
 * @param targetVerseId The BBCCCVVV-encoded verse being referenced.
 * @param type Reference type (e.g., "direct", "thematic", "parallel").
 * @param confidence Confidence score (0.0–1.0) for algorithmic references.
 */
data class CrossReference(
    val id: Long,
    val sourceVerseId: Long,
    val targetVerseId: Long,
    val type: String,
    val confidence: Double
)
