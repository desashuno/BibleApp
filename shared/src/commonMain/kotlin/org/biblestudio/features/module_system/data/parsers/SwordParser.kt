package org.biblestudio.features.module_system.data.parsers

import io.github.aakira.napier.Napier

/**
 * Sword module reader for Bible module import.
 *
 * Reads Sword `.conf` configuration files and extracts module metadata.
 * The actual verse data decoding from compressed Sword data files is
 * a simplified implementation suitable for common module formats.
 *
 * Full Sword module support (compressed ZIP/LZSS, cipher keys) requires
 * platform-specific decompression which is deferred in this implementation.
 */
object SwordParser {

    /**
     * Parses a Sword `.conf` file content to extract module metadata.
     *
     * @param confContent The full text of the `.conf` file.
     * @return A map of configuration keys to values.
     */
    fun parseConf(confContent: String): Map<String, String> {
        val config = mutableMapOf<String, String>()
        var currentKey = ""

        for (line in confContent.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            if (trimmed.contains("=")) {
                val (key, value) = trimmed.split("=", limit = 2)
                currentKey = key.trim()
                config[currentKey] = value.trim()
            } else if (currentKey.isNotEmpty()) {
                // Continuation of previous multi-line value
                config[currentKey] = (config[currentKey] ?: "") + " " + trimmed
            }
        }

        return config
    }

    /**
     * Extracts module metadata from a parsed conf map.
     */
    fun extractMetadata(conf: Map<String, String>): SwordMetadata {
        return SwordMetadata(
            name = conf["Description"] ?: conf["ModDrv"] ?: "Unknown Module",
            abbreviation = conf["Abbreviation"] ?: conf["ModuleName"] ?: "UNK",
            language = conf["Lang"] ?: "en",
            version = conf["Version"] ?: "1.0",
            description = conf["About"] ?: "",
            encoding = conf["Encoding"] ?: "UTF-8",
            sourceType = conf["SourceType"] ?: "OSIS",
            moduleType = resolveSwordModuleType(conf)
        )
    }

    /**
     * Parses raw Sword verse data (plain text format only).
     *
     * This handles the simple case where verse data is stored as
     * plain text lines in OSIS or ThML format within a Sword module.
     *
     * @param verseData The raw verse data content.
     * @param metadata Module metadata from conf parsing.
     */
    fun parseVerseData(verseData: String, metadata: SwordMetadata): ParseResult {
        return when (metadata.sourceType.uppercase()) {
            "OSIS" -> OsisParser.parse(verseData, metadata.name, metadata.abbreviation)
            "THML", "PLAIN" -> parsePlainVerseData(verseData, metadata)
            else -> {
                Napier.w("Unsupported Sword sourceType: ${metadata.sourceType}, falling back to plain parse")
                parsePlainVerseData(verseData, metadata)
            }
        }
    }

    /**
     * Validates Sword module metadata.
     */
    fun validate(conf: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        if (!conf.containsKey("Description") && !conf.containsKey("ModDrv")) {
            errors.add("Missing module description in .conf")
        }

        return if (errors.isEmpty()) ValidationResult.valid() else ValidationResult.invalid(errors)
    }

    private fun parsePlainVerseData(data: String, metadata: SwordMetadata): ParseResult {
        val books = mutableListOf<ParsedBook>()
        // Simple line-by-line parsing for plain text modules
        // Each line: "BookName Chapter:Verse Text..."
        val linePattern = Regex("""^(.+?)\s+(\d+):(\d+)\s+(.+)$""")

        val bookVerses = mutableMapOf<String, MutableMap<Int, MutableList<ParsedVerse>>>()

        for (line in data.lines()) {
            val match = linePattern.find(line.trim()) ?: continue
            val bookName = match.groupValues[1]
            val chapter = match.groupValues[2].toInt()
            val verse = match.groupValues[3].toInt()
            val text = match.groupValues[4]

            bookVerses
                .getOrPut(bookName) { mutableMapOf() }
                .getOrPut(chapter) { mutableListOf() }
                .add(ParsedVerse(0, chapter, verse, text))
        }

        var bookNum = 1
        for ((name, chapters) in bookVerses) {
            books.add(
                ParsedBook(
                    number = bookNum,
                    name = name,
                    testament = if (bookNum <= OT_BOOK_COUNT) "OT" else "NT",
                    chapters = chapters.mapValues { it.value.toList() }
                )
            )
            bookNum++
        }

        return ParseResult(
            moduleName = metadata.name,
            abbreviation = metadata.abbreviation,
            language = metadata.language,
            books = books
        )
    }

    private fun resolveSwordModuleType(conf: Map<String, String>): String {
        val modDrv = conf["ModDrv"]?.lowercase() ?: ""
        return when {
            modDrv.contains("rawtext") || modDrv.contains("ztext") -> "bible"
            modDrv.contains("rawcom") || modDrv.contains("zcom") -> "commentary"
            modDrv.contains("rawld") || modDrv.contains("zld") -> "dictionary"
            modDrv.contains("rawgenbook") -> "general"
            else -> "bible"
        }
    }

    private const val OT_BOOK_COUNT = 39
}

/**
 * Metadata extracted from a Sword .conf file.
 */
data class SwordMetadata(
    val name: String,
    val abbreviation: String,
    val language: String,
    val version: String,
    val description: String,
    val encoding: String,
    val sourceType: String,
    val moduleType: String
)
