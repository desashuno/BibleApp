package org.biblestudio.features.bible_reader.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bible_reader.data.mappers.toBible
import org.biblestudio.features.bible_reader.data.mappers.toBook
import org.biblestudio.features.bible_reader.data.mappers.toChapter
import org.biblestudio.features.bible_reader.data.mappers.toVerse
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Chapter
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository

internal class BibleRepositoryImpl(
    private val database: BibleStudioDatabase
) : BibleRepository {

    override suspend fun getAvailableBibles(): Result<List<Bible>> = runCatching {
        database.bibleQueries
            .allBibles()
            .executeAsList()
            .map { it.toBible() }
    }

    override suspend fun getBooks(bibleId: Long): Result<List<Book>> = runCatching {
        database.bibleQueries
            .allBooksForBible(bibleId)
            .executeAsList()
            .map { it.toBook() }
    }

    override suspend fun getChapters(bookId: Long): Result<List<Chapter>> = runCatching {
        database.bibleQueries
            .chaptersForBook(bookId)
            .executeAsList()
            .map { it.toChapter() }
    }

    override suspend fun getVerses(bookId: Long, chapter: Long): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .versesForChapter(bookId, chapter)
            .executeAsList()
            .map { it.toVerse() }
    }

    override suspend fun getVerseByGlobalId(globalVerseId: Long): Result<Verse?> = runCatching {
        database.bibleQueries
            .verseByGlobalId(globalVerseId)
            .executeAsOneOrNull()
            ?.toVerse()
    }

    override suspend fun getVersesInRange(startId: Long, endId: Long): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .versesInRange(startId, endId)
            .executeAsList()
            .map { it.toVerse() }
    }

    override suspend fun getVersesForBook(bookId: Long): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .versesForBook(bookId)
            .executeAsList()
            .map { it.toVerse() }
    }

    override suspend fun searchVerses(query: String, maxResults: Long): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .searchVerses(query, maxResults)
            .executeAsList()
            .map { it.toVerse() }
    }

    override suspend fun getActiveBibles(): Result<List<Bible>> = runCatching {
        database.bibleQueries
            .activeBibles()
            .executeAsList()
            .map { it.toBible() }
    }

    override suspend fun getAvailableBiblesByLanguage(languageCode: String): Result<List<Bible>> = runCatching {
        database.bibleQueries
            .biblesByLanguage(languageCode)
            .executeAsList()
            .map { it.toBible() }
    }

    override suspend fun getActiveBiblesByLanguage(languageCode: String): Result<List<Bible>> = runCatching {
        database.bibleQueries
            .activeBiblesByLanguage(languageCode)
            .executeAsList()
            .map { it.toBible() }
    }

    override suspend fun getNextVerseId(currentId: Long): Result<Long?> = runCatching {
        database.bibleQueries.nextGlobalVerseId(currentId).executeAsOneOrNull()?.next_id
    }

    override suspend fun getPreviousVerseId(currentId: Long): Result<Long?> = runCatching {
        database.bibleQueries.previousGlobalVerseId(currentId).executeAsOneOrNull()?.prev_id
    }

    override fun watchBibles(): Flow<List<Bible>> = database.bibleQueries
        .allBibles()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toBible() } }
}
