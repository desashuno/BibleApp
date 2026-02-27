package org.biblestudio.features.bible_reader.domain.entities

/**
 * Represents a single Bible verse.
 *
 * @param id Auto-generated database ID.
 * @param chapterId FK to the parent [Chapter].
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param verseNumber The verse number within its chapter (1-based).
 * @param text Plain text content of the verse.
 * @param htmlText Optional HTML-formatted text for rich display.
 */
data class Verse(
    val id: Long,
    val chapterId: Long,
    val globalVerseId: Long,
    val verseNumber: Long,
    val text: String,
    val htmlText: String? = null
)
