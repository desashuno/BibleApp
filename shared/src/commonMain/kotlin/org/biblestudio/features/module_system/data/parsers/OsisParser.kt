package org.biblestudio.features.module_system.data.parsers

import io.github.aakira.napier.Napier

/**
 * OSIS XML parser for Bible module import.
 *
 * Parses elements: `<verse>`, `<chapter>`, `<note>`, `<reference>`,
 * `<w lemma="">` for Strong's number extraction.
 *
 * This is a lightweight streaming parser that does not require a full XML
 * DOM library — it uses regex-based tag extraction suitable for well-formed
 * OSIS documents.
 */
object OsisParser {

    private val VERSE_TAG = Regex("""<verse\s+osisID="([^"]+)"[^>]*>(.*?)</verse>""", RegexOption.DOT_MATCHES_ALL)
    private val LEMMA_ATTR = Regex("""<w\s+lemma="([^"]+)"[^>]*>([^<]*)</w>""")
    private val NOTE_TAG = Regex("""<note[^>]*>.*?</note>""", RegexOption.DOT_MATCHES_ALL)
    private val TAG_PATTERN = Regex("""<[^>]+>""")

    /**
     * Parses an OSIS XML string into a [ParseResult].
     *
     * @param xml The full OSIS XML content.
     * @param moduleName Human-readable module name.
     * @param abbreviation Module abbreviation (e.g., "KJV").
     */
    @Suppress("LoopWithTooManyJumpStatements")
    fun parse(xml: String, moduleName: String = "Unknown", abbreviation: String = "UNK"): ParseResult {
        val errors = mutableListOf<String>()
        val bookMap = mutableMapOf<Int, MutableMap<Int, MutableList<ParsedVerse>>>()

        for (match in VERSE_TAG.findAll(xml)) {
            val osisId = match.groupValues[1]
            val content = match.groupValues[2]

            val parts = osisId.split(".")
            if (parts.size < 3) {
                errors.add("Invalid osisID: $osisId")
                continue
            }

            val bookNum = osisBookToNumber(parts[0])
            val chapter = parts[1].toIntOrNull() ?: continue
            val verse = parts[2].toIntOrNull() ?: continue

            val lemmas = LEMMA_ATTR.findAll(content).map { it.groupValues[1] }.toList()
            val plainText = content
                .replace(NOTE_TAG, "")
                .replace(TAG_PATTERN, "")
                .trim()

            val parsedVerse = ParsedVerse(
                bookNumber = bookNum,
                chapterNumber = chapter,
                verseNumber = verse,
                text = plainText,
                htmlText = content.trim(),
                lemmaRefs = lemmas
            )

            bookMap
                .getOrPut(bookNum) { mutableMapOf() }
                .getOrPut(chapter) { mutableListOf() }
                .add(parsedVerse)
        }

        val books = bookMap.entries.sortedBy { it.key }.map { (bookNum, chapters) ->
            ParsedBook(
                number = bookNum,
                name = osisNumberToName(bookNum),
                testament = if (bookNum <= OT_BOOK_COUNT) "OT" else "NT",
                chapters = chapters.mapValues { it.value.toList() }
            )
        }

        Napier.i("OSIS parse: ${books.size} books, ${errors.size} errors")

        return ParseResult(
            moduleName = moduleName,
            abbreviation = abbreviation,
            language = "en",
            books = books,
            errors = errors
        )
    }

    /**
     * Validates a parse result for completeness.
     */
    fun validate(result: ParseResult): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (result.books.isEmpty()) {
            errors.add("No books found in OSIS document")
        }

        result.books.forEach { book ->
            if (book.chapters.isEmpty()) {
                warnings.add("Book ${book.name} has no chapters")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult(isValid = true, errors = emptyList(), warnings = warnings)
        } else {
            ValidationResult(isValid = false, errors = errors, warnings = warnings)
        }
    }

    private const val OT_BOOK_COUNT = 39

    @Suppress("CyclomaticComplexMethod", "MagicNumber", "LongMethod")
    private fun osisBookToNumber(osisBook: String): Int = when (osisBook) {
        "Gen" -> 1
        "Exod" -> 2
        "Lev" -> 3
        "Num" -> 4
        "Deut" -> 5
        "Josh" -> 6
        "Judg" -> 7
        "Ruth" -> 8
        "1Sam" -> 9
        "2Sam" -> 10
        "1Kgs" -> 11
        "2Kgs" -> 12
        "1Chr" -> 13
        "2Chr" -> 14
        "Ezra" -> 15
        "Neh" -> 16
        "Esth" -> 17
        "Job" -> 18
        "Ps" -> 19
        "Prov" -> 20
        "Eccl" -> 21
        "Song" -> 22
        "Isa" -> 23
        "Jer" -> 24
        "Lam" -> 25
        "Ezek" -> 26
        "Dan" -> 27
        "Hos" -> 28
        "Joel" -> 29
        "Amos" -> 30
        "Obad" -> 31
        "Jonah" -> 32
        "Mic" -> 33
        "Nah" -> 34
        "Hab" -> 35
        "Zeph" -> 36
        "Hag" -> 37
        "Zech" -> 38
        "Mal" -> 39
        "Matt" -> 40
        "Mark" -> 41
        "Luke" -> 42
        "John" -> 43
        "Acts" -> 44
        "Rom" -> 45
        "1Cor" -> 46
        "2Cor" -> 47
        "Gal" -> 48
        "Eph" -> 49
        "Phil" -> 50
        "Col" -> 51
        "1Thess" -> 52
        "2Thess" -> 53
        "1Tim" -> 54
        "2Tim" -> 55
        "Titus" -> 56
        "Phlm" -> 57
        "Heb" -> 58
        "Jas" -> 59
        "1Pet" -> 60
        "2Pet" -> 61
        "1John" -> 62
        "2John" -> 63
        "3John" -> 64
        "Jude" -> 65
        "Rev" -> 66
        else -> 0
    }

    @Suppress("CyclomaticComplexMethod", "MagicNumber", "LongMethod")
    private fun osisNumberToName(num: Int): String = when (num) {
        1 -> "Genesis"
        2 -> "Exodus"
        3 -> "Leviticus"
        4 -> "Numbers"
        5 -> "Deuteronomy"
        6 -> "Joshua"
        7 -> "Judges"
        8 -> "Ruth"
        9 -> "1 Samuel"
        10 -> "2 Samuel"
        11 -> "1 Kings"
        12 -> "2 Kings"
        13 -> "1 Chronicles"
        14 -> "2 Chronicles"
        15 -> "Ezra"
        16 -> "Nehemiah"
        17 -> "Esther"
        18 -> "Job"
        19 -> "Psalms"
        20 -> "Proverbs"
        21 -> "Ecclesiastes"
        22 -> "Song of Solomon"
        23 -> "Isaiah"
        24 -> "Jeremiah"
        25 -> "Lamentations"
        26 -> "Ezekiel"
        27 -> "Daniel"
        28 -> "Hosea"
        29 -> "Joel"
        30 -> "Amos"
        31 -> "Obadiah"
        32 -> "Jonah"
        33 -> "Micah"
        34 -> "Nahum"
        35 -> "Habakkuk"
        36 -> "Zephaniah"
        37 -> "Haggai"
        38 -> "Zechariah"
        39 -> "Malachi"
        40 -> "Matthew"
        41 -> "Mark"
        42 -> "Luke"
        43 -> "John"
        44 -> "Acts"
        45 -> "Romans"
        46 -> "1 Corinthians"
        47 -> "2 Corinthians"
        48 -> "Galatians"
        49 -> "Ephesians"
        50 -> "Philippians"
        51 -> "Colossians"
        52 -> "1 Thessalonians"
        53 -> "2 Thessalonians"
        54 -> "1 Timothy"
        55 -> "2 Timothy"
        56 -> "Titus"
        57 -> "Philemon"
        58 -> "Hebrews"
        59 -> "James"
        60 -> "1 Peter"
        61 -> "2 Peter"
        62 -> "1 John"
        63 -> "2 John"
        64 -> "3 John"
        65 -> "Jude"
        66 -> "Revelation"
        else -> "Unknown"
    }
}
