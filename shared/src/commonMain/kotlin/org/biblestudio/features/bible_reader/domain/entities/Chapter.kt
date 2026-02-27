package org.biblestudio.features.bible_reader.domain.entities

/**
 * Represents a chapter within a book.
 *
 * @param id Auto-generated database ID.
 * @param bookId FK to the parent [Book].
 * @param chapterNumber The chapter number (1-based).
 * @param verseCount Total number of verses in this chapter.
 */
data class Chapter(
    val id: Long,
    val bookId: Long,
    val chapterNumber: Long,
    val verseCount: Long
)
