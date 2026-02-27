package org.biblestudio.features.bible_reader.domain.entities

/**
 * Represents a book of the Bible (e.g., Genesis, Matthew).
 *
 * @param id Auto-generated database ID.
 * @param bibleId FK to the parent [Bible].
 * @param bookNumber Canonical book number (1–66).
 * @param name Display name (e.g., "Genesis").
 * @param testament "OT" or "NT".
 */
data class Book(
    val id: Long,
    val bibleId: Long,
    val bookNumber: Long,
    val name: String,
    val testament: String,
    val chapterCount: Long = 0
)
