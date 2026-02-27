package org.biblestudio.features.bible_reader.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Chapter
import org.biblestudio.features.bible_reader.domain.entities.Verse

/**
 * Provides read access to Bible text data: versions, books, chapters, and verses.
 */
interface BibleRepository {

    /** Returns all available Bible versions, ordered by name. */
    suspend fun getAvailableBibles(): Result<List<Bible>>

    /** Returns all books for a given Bible [bibleId], ordered by book number. */
    suspend fun getBooks(bibleId: Long): Result<List<Book>>

    /** Returns all chapters for a given [bookId], ordered by chapter number. */
    suspend fun getChapters(bookId: Long): Result<List<Chapter>>

    /** Returns all verses for a chapter identified by [bookId] and [chapter] number. */
    suspend fun getVerses(bookId: Long, chapter: Long): Result<List<Verse>>

    /** Returns a single verse by its global verse ID. */
    suspend fun getVerseByGlobalId(globalVerseId: Long): Result<Verse?>

    /** Returns verses within a global-ID range (inclusive). */
    suspend fun getVersesInRange(startId: Long, endId: Long): Result<List<Verse>>

    /** Full-text search across verse text. */
    suspend fun searchVerses(query: String, maxResults: Long = 100): Result<List<Verse>>

    /** Reactive stream of available Bible versions. */
    fun watchBibles(): Flow<List<Bible>>
}
