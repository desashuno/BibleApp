package org.biblestudio.core.data_manager.handlers

import io.github.aakira.napier.Napier
import org.biblestudio.core.data_manager.DataModuleHandler
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.core.data_manager.parsers.OsisParser
import org.biblestudio.core.data_manager.parsers.ParseResult
import org.biblestudio.core.data_manager.parsers.SwordParser
import org.biblestudio.core.data_manager.parsers.UsfmParser
import org.biblestudio.database.BibleStudioDatabase

/**
 * Handles Bible module installation using OSIS/USFM/Sword parsers.
 */
internal class BibleModuleHandler(
    private val database: BibleStudioDatabase
) : DataModuleHandler {

    override val supportedType: DataModuleType = DataModuleType.Bible

    override suspend fun install(descriptor: DataModuleDescriptor, progressCallback: (Float) -> Unit): Result<Unit> =
        runCatching {
            Napier.i("Installing Bible module: ${descriptor.name}")

            progressCallback(0.1f)
            val parseResult = parseDescriptor(descriptor)
            require(parseResult.books.isNotEmpty()) {
                "Parsed result is empty for module '${descriptor.moduleId}'"
            }

            progressCallback(0.35f)
            persistParsedBible(descriptor, parseResult, progressCallback)

            progressCallback(1.0f)
            Napier.i("Bible module installed: ${descriptor.moduleId}")
        }

    override suspend fun remove(descriptor: DataModuleDescriptor): Result<Unit> = runCatching {
        Napier.i("Removing Bible module: ${descriptor.name}")
        val abbreviation = resolveAbbreviation(descriptor)
        database.bibleQueries
            .allBibles()
            .executeAsList()
            .filter { it.abbreviation.equals(abbreviation, ignoreCase = true) }
            .forEach { bible -> database.bibleQueries.deleteBible(bible.id) }
    }

    override suspend fun validate(descriptor: DataModuleDescriptor): Result<Boolean> = runCatching {
        val abbreviation = resolveAbbreviation(descriptor)
        database.bibleQueries
            .allBibles()
            .executeAsList()
            .any { it.abbreviation.equals(abbreviation, ignoreCase = true) }
    }

    private fun parseDescriptor(descriptor: DataModuleDescriptor): ParseResult {
        val format = (metadataValue(descriptor.metadata, "format") ?: "osis").lowercase()
        val content = resolveInlineContent(descriptor)

        return when (format) {
            "osis" -> OsisParser.parse(content, descriptor.name, resolveAbbreviation(descriptor))
            "usfm" -> {
                val bookId = metadataValue(descriptor.metadata, "bookId") ?: "GEN"
                UsfmParser.parse(
                    books = mapOf(bookId to content),
                    moduleName = descriptor.name,
                    abbreviation = resolveAbbreviation(descriptor)
                )
            }
            "sword" -> {
                val confContent = metadataValue(descriptor.metadata, "swordConf")
                    ?: error("Missing 'swordConf' in metadata for sword format")
                val confMap = SwordParser.parseConf(unescape(confContent))
                val swordMetadata = SwordParser.extractMetadata(confMap)
                SwordParser.parseVerseData(content, swordMetadata)
            }
            else -> error("Unsupported Bible module format '$format' for ${descriptor.moduleId}")
        }
    }

    private fun persistParsedBible(
        descriptor: DataModuleDescriptor,
        parseResult: ParseResult,
        progressCallback: (Float) -> Unit
    ) {
        val abbreviation = resolveAbbreviation(descriptor)
        val textDirection = if (descriptor.language.lowercase() in RTL_LANGS) "rtl" else "ltr"

        database.transaction {
            database.bibleQueries
                .allBibles()
                .executeAsList()
                .filter { it.abbreviation.equals(abbreviation, ignoreCase = true) }
                .forEach { bible -> database.bibleQueries.deleteBible(bible.id) }

            database.bibleQueries.insertBible(
                abbreviation = abbreviation,
                name = descriptor.name,
                language = descriptor.language,
                textDirection = textDirection
            )

            val bibleId = database.bibleQueries
                .allBibles()
                .executeAsList()
                .filter { it.abbreviation.equals(abbreviation, ignoreCase = true) }
                .maxByOrNull { it.id }
                ?.id
                ?: error("Failed to resolve inserted bible id for $abbreviation")

            val totalBooks = parseResult.books.size.coerceAtLeast(1)
            parseResult.books.sortedBy { it.number }.forEachIndexed { index, book ->
                database.bibleQueries.insertBook(
                    bibleId = bibleId,
                    bookNumber = book.number.toLong(),
                    name = book.name,
                    testament = book.testament
                )

                val bookId = database.bibleQueries
                    .allBooksForBible(bibleId)
                    .executeAsList()
                    .firstOrNull { it.book_number == book.number.toLong() }
                    ?.id
                    ?: error("Failed to resolve inserted book id for ${book.name}")

                book.chapters.toSortedMap().forEach { (chapterNumber, verses) ->
                    database.bibleQueries.insertChapter(
                        bookId = bookId,
                        chapterNumber = chapterNumber.toLong(),
                        verseCount = verses.size.toLong()
                    )

                    val chapterId = database.bibleQueries
                        .chaptersForBook(bookId)
                        .executeAsList()
                        .firstOrNull { it.chapter_number == chapterNumber.toLong() }
                        ?.id
                        ?: error("Failed to resolve chapter id for ${book.name} $chapterNumber")

                    verses.sortedBy { it.verseNumber }.forEach { verse ->
                        database.bibleQueries.insertVerse(
                            chapterId = chapterId,
                            globalVerseId = computeGlobalVerseId(
                                book = book.number,
                                chapter = chapterNumber,
                                verse = verse.verseNumber
                            ),
                            verseNumber = verse.verseNumber.toLong(),
                            text = verse.text,
                            htmlText = verse.htmlText
                        )
                    }
                }

                val progress = 0.35f + (0.6f * (index + 1) / totalBooks)
                progressCallback(progress)
            }
        }
    }

    private fun resolveInlineContent(descriptor: DataModuleDescriptor): String {
        val metadataContent = metadataValue(descriptor.metadata, "content")
            ?: metadataValue(descriptor.metadata, "data")
            ?: metadataValue(descriptor.metadata, "osis")
            ?: metadataValue(descriptor.metadata, "usfm")

        if (metadataContent != null) {
            return unescape(metadataContent)
        }

        if (descriptor.sourceUrl.startsWith("inline:")) {
            return descriptor.sourceUrl.removePrefix("inline:")
        }

        error(
            "Bible module '${descriptor.moduleId}' is missing inline source content. " +
                "Provide metadata.content (or data/osis/usfm) or sourceUrl='inline:...'."
        )
    }

    private fun resolveAbbreviation(descriptor: DataModuleDescriptor): String {
        val fromMetadata = metadataValue(descriptor.metadata, "abbreviation")?.trim()
        if (!fromMetadata.isNullOrEmpty()) return fromMetadata

        return descriptor.moduleId
            .substringAfter("bible-", descriptor.moduleId)
            .replace("-", "")
            .uppercase()
            .ifBlank { "UNK" }
    }

    private fun metadataValue(metadata: String, key: String): String? {
        val regex = Regex("\"$key\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"")
        return regex.find(metadata)?.groupValues?.get(1)
    }

    private fun computeGlobalVerseId(book: Int, chapter: Int, verse: Int): Long {
        return (book * 1_000_000L) + (chapter * 1_000L) + verse
    }

    private fun unescape(value: String): String {
        return value
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
    }

    private companion object {
        val RTL_LANGS = setOf("ar", "he", "fa", "ur")
    }
}
