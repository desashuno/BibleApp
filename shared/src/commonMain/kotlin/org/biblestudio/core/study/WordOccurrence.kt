package org.biblestudio.core.study

/**
 * Records where a specific word (by Strong's number) occurs in the Bible.
 *
 * @param id Auto-generated database ID.
 * @param strongsNumber Strong's reference.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param wordPosition 0-based word position within the verse.
 */
data class WordOccurrence(
    val id: Long,
    val strongsNumber: String,
    val globalVerseId: Long,
    val wordPosition: Long
)
