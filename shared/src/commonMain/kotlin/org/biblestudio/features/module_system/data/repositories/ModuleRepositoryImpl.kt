package org.biblestudio.features.module_system.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.module_system.data.mappers.toInstalledModule
import org.biblestudio.features.module_system.data.parsers.ParseResult
import org.biblestudio.features.module_system.domain.entities.InstalledModule
import org.biblestudio.features.module_system.domain.entities.ModuleSource
import org.biblestudio.features.module_system.domain.repositories.ModuleRepository

/**
 * Default [ModuleRepository] backed by SQLDelight and module parsers.
 */
internal class ModuleRepositoryImpl(
    private val database: BibleStudioDatabase
) : ModuleRepository {

    override suspend fun getInstalledModules(): Result<List<InstalledModule>> = runCatching {
        database.moduleQueries
            .allModules()
            .executeAsList()
            .map { it.toInstalledModule() }
    }

    override suspend fun getModulesByType(type: String): Result<List<InstalledModule>> = runCatching {
        database.moduleQueries
            .modulesByType(type)
            .executeAsList()
            .map { it.toInstalledModule() }
    }

    override suspend fun getModule(uuid: String): Result<InstalledModule?> = runCatching {
        database.moduleQueries
            .moduleByUuid(uuid)
            .executeAsOneOrNull()
            ?.toInstalledModule()
    }

    @Suppress("MagicNumber")
    override suspend fun installModule(
        source: ModuleSource,
        progressCallback: ((Float) -> Unit)?
    ): Result<InstalledModule> = runCatching {
        progressCallback?.invoke(0.1f)

        val sourceType = when (source) {
            is ModuleSource.Sword -> "sword"
            is ModuleSource.Osis -> "osis"
            is ModuleSource.Usfm -> "usfm"
            is ModuleSource.CustomZip -> "zip"
        }

        val uuid = generateModuleUuid()
        val name = resolveModuleName(source)
        val abbreviation = resolveAbbreviation(source)

        progressCallback?.invoke(0.5f)

        database.moduleQueries.insertModule(
            uuid = uuid,
            name = name,
            abbreviation = abbreviation,
            language = "en",
            type = "bible",
            version = "1.0",
            size_bytes = 0,
            description = "Imported from $sourceType",
            source_type = sourceType
        )

        progressCallback?.invoke(1.0f)

        database.moduleQueries
            .moduleByUuid(uuid)
            .executeAsOne()
            .toInstalledModule()
    }

    override suspend fun removeModule(uuid: String): Result<Unit> = runCatching {
        database.moduleQueries.softDeleteModule(uuid)
    }

    override suspend fun purgeModule(uuid: String): Result<Unit> = runCatching {
        database.moduleQueries.hardDeleteModule(uuid)
    }

    /**
     * Inserts morphology and cross-reference data extracted from a [ParseResult].
     * Called after initial module metadata has been persisted.
     */
    @Suppress("NestedBlockDepth")
    internal fun insertParsedModuleData(result: ParseResult) {
        database.transaction {
            for (book in result.books) {
                for ((chapterNum, verses) in book.chapters) {
                    for (verse in verses) {
                        val globalVerseId = computeGlobalVerseId(
                            book.number,
                            chapterNum,
                            verse.verseNumber
                        )
                        verse.lemmaRefs.forEachIndexed { idx, strongs ->
                            database.studyQueries.insertMorphology(
                                globalVerseId = globalVerseId,
                                wordPosition = idx.toLong() + 1,
                                strongsNumber = strongs,
                                parsingCode = "",
                                surfaceForm = "",
                                lemma = "",
                                gloss = ""
                            )
                        }
                    }
                }
            }
        }
    }

    private fun generateModuleUuid(): String {
        // Simple UUID generation using random bytes
        val chars = "abcdef0123456789"
        val segments = listOf(8, 4, 4, 4, 12)
        return segments.joinToString("-") { len ->
            (1..len).map { chars.random() }.joinToString("")
        }
    }

    private fun resolveModuleName(source: ModuleSource): String = when (source) {
        is ModuleSource.Sword -> source.confPath.substringAfterLast("/").removeSuffix(".conf")
        is ModuleSource.Osis -> source.xmlPath.substringAfterLast("/").removeSuffix(".xml")
        is ModuleSource.Usfm -> source.directoryPath.substringAfterLast("/")
        is ModuleSource.CustomZip -> source.zipPath.substringAfterLast("/").removeSuffix(".zip")
    }

    private fun resolveAbbreviation(source: ModuleSource): String =
        resolveModuleName(source).take(MAX_ABBREVIATION_LENGTH).uppercase()

    companion object {
        private const val MAX_ABBREVIATION_LENGTH = 10
        private const val BOOK_MULTIPLIER = 1_000_000L
        private const val CHAPTER_MULTIPLIER = 1_000L
    }

    private fun computeGlobalVerseId(bookNumber: Int, chapter: Int, verse: Int): Long =
        bookNumber.toLong() * BOOK_MULTIPLIER + chapter.toLong() * CHAPTER_MULTIPLIER + verse.toLong()
}
