package org.biblestudio.core.util

import org.biblestudio.core.study.LexiconEntry
import org.biblestudio.database.BibleStudioDatabase

/**
 * FTS-with-fallback lexicon search shared by WordStudyRepositoryImpl and SearchRepositoryImpl.
 *
 * Tries FTS5 first; falls back to LIKE-based search on FTS special chars or FTS failure.
 */
fun searchLexiconWithFallback(
    database: BibleStudioDatabase,
    query: String,
    maxResults: Long
): List<LexiconEntry> {
    val mapper = { row: migrations.Lexicon_entries ->
        LexiconEntry(
            strongsNumber = row.strongs_number,
            originalWord = row.original_word,
            transliteration = row.transliteration,
            definition = row.definition,
            usageNotes = row.usage_notes,
            glossEs = row.gloss_es,
        )
    }
    return if (hasFtsSpecialChars(query)) {
        database.studyQueries
            .searchLexiconFallback(query, maxResults)
            .executeAsList()
            .map(mapper)
    } else {
        try {
            database.studyQueries
                .searchLexiconFts(query, maxResults)
                .executeAsList()
                .map(mapper)
        } catch (_: Exception) {
            database.studyQueries
                .searchLexiconFallback(query, maxResults)
                .executeAsList()
                .map(mapper)
        }
    }
}
