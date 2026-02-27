package org.biblestudio.features.passage_guide.domain.entities

/**
 * A passage outline entry linking a verse range to a structural heading.
 *
 * @param id Auto-generated database ID.
 * @param globalVerseStart Start of the verse range (BBCCCVVV).
 * @param globalVerseEnd End of the verse range (BBCCCVVV).
 * @param title Outline heading text.
 * @param content Optional body/notes for the outline entry.
 * @param source Attribution or source reference.
 */
data class Outline(
    val id: Long,
    val globalVerseStart: Long,
    val globalVerseEnd: Long,
    val title: String,
    val content: String,
    val source: String
)
