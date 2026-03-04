package org.biblestudio.features.bible_reader.domain.entities

/**
 * Holds parallel verse texts from multiple Bible versions for comparison.
 *
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param versions Map of Bible abbreviation → verse payload.
 */
data class VersionComparison(
    val globalVerseId: Long,
    val versions: Map<String, VersionVerse>
)

/**
 * Verse payload for text-comparison entries.
 */
data class VersionVerse(
    val text: String,
    val htmlText: String? = null
)
