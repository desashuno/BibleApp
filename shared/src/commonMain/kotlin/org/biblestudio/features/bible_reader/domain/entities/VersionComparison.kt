package org.biblestudio.features.bible_reader.domain.entities

/**
 * Holds parallel verse texts from multiple Bible versions for comparison.
 *
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param versions Map of Bible abbreviation → verse text.
 */
data class VersionComparison(
    val globalVerseId: Long,
    val versions: Map<String, String>
)
