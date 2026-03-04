package org.biblestudio.core.data_manager.parsers

import io.github.aakira.napier.Napier

/**
 * USFM (Unified Standard Format Markers) parser for Bible module import.
 *
 * Supported markers:
 * - `\id` — book identification
 * - `\h` — book header/name
 * - `\c` — chapter number
 * - `\v` — verse number and text
 * - `\p` — paragraph break
 * - `\f ... \f*` — footnotes (stripped)
 * - `\x ... \x*` — cross-references (stripped)
 * - `\w ... \w*` — word-level markup with Strong's numbers
 */
object UsfmParser {

    private val CHAPTER_MARKER = Regex("""\\c\s+(\d+)""")
    private val VERSE_MARKER = Regex("""\\v\s+(\d+)\s+""")
    private val WORD_MARKER = Regex("""\\w\s+([^|]*)\|[^\\]*\\w\*""")
    private val STRONGS_ATTR = Regex("""strong="([^"]+)"""")
    private val FOOTNOTE = Regex("""\\f\s+.*?\\f\*""", RegexOption.DOT_MATCHES_ALL)
    private val CROSS_REF = Regex("""\\x\s+.*?\\x\*""", RegexOption.DOT_MATCHES_ALL)
    private val MARKER = Regex("""\\[a-z]+\d?\s*""")
    private val ID_MARKER = Regex("""\\id\s+(\S+)""")
    private val HEADER_MARKER = Regex("""\\h\s+(.+)""")
    private val WORDS_OF_JESUS_MARKER = Regex("""\\wj\s+(.*?)\\wj\*""")
    private val HTML_TAG = Regex("""<[^>]+>""")

    /**
     * Parses a set of USFM book contents into a [ParseResult].
     *
     * @param books Map of book identifier to USFM file content.
     * @param moduleName Human-readable module name.
     * @param abbreviation Module abbreviation.
     */
    fun parse(books: Map<String, String>, moduleName: String = "Unknown", abbreviation: String = "UNK"): ParseResult {
        val errors = mutableListOf<String>()
        val parsedBooks = mutableListOf<ParsedBook>()

        for ((_, content) in books) {
            val book = parseBook(content, errors)
            if (book != null) {
                parsedBooks.add(book)
            }
        }

        parsedBooks.sortBy { it.number }

        Napier.i("USFM parse: ${parsedBooks.size} books, ${errors.size} errors")

        return ParseResult(
            moduleName = moduleName,
            abbreviation = abbreviation,
            language = "en",
            books = parsedBooks,
            errors = errors
        )
    }

    /**
     * Parses a single USFM book content string.
     */
    fun parseBook(content: String, errors: MutableList<String>): ParsedBook? {
        val idMatch = ID_MARKER.find(content)
        val bookId = idMatch?.groupValues?.get(1) ?: run {
            errors.add("Missing \\id marker in USFM content")
            return null
        }

        val bookNum = usfmBookToNumber(bookId)
        if (bookNum == 0) {
            errors.add("Unknown USFM book ID: $bookId")
            return null
        }

        val headerMatch = HEADER_MARKER.find(content)
        val bookName = headerMatch?.groupValues?.get(1)?.trim() ?: usfmNumberToName(bookNum)

        val chapters = mutableMapOf<Int, MutableList<ParsedVerse>>()
        var currentChapter = 0

        val lines = content.lines()
        for (line in lines) {
            val chapterMatch = CHAPTER_MARKER.find(line)
            if (chapterMatch != null) {
                currentChapter = chapterMatch.groupValues[1].toInt()
                continue
            }

            val verseMatch = VERSE_MARKER.find(line)
            if (verseMatch != null && currentChapter > 0) {
                val verseNum = verseMatch.groupValues[1].toInt()
                val rawText = line.substring(verseMatch.range.last + 1)

                // Extract Strong's references
                val lemmas = WORD_MARKER.findAll(rawText)
                    .flatMap { wordMatch ->
                        STRONGS_ATTR.findAll(wordMatch.value).map { it.groupValues[1] }
                    }
                    .toList()

                // Clean text
                val htmlText = rawText
                    .replace(FOOTNOTE, "")
                    .replace(CROSS_REF, "")
                    .replace(WORDS_OF_JESUS_MARKER) { "<wj>${it.groupValues[1].trim()}</wj>" }
                    .replace(WORD_MARKER) { it.groupValues[1] }
                    .replace(MARKER, "")
                    .trim()

                val plainText = htmlText
                    .replace(FOOTNOTE, "")
                    .replace(CROSS_REF, "")
                    .replace(HTML_TAG, "")
                    .trim()

                val verse = ParsedVerse(
                    bookNumber = bookNum,
                    chapterNumber = currentChapter,
                    verseNumber = verseNum,
                    text = plainText,
                    htmlText = htmlText.takeIf { it.contains("<wj>", ignoreCase = true) },
                    lemmaRefs = lemmas
                )

                chapters.getOrPut(currentChapter) { mutableListOf() }.add(verse)
            }
        }

        return ParsedBook(
            number = bookNum,
            name = bookName,
            testament = if (bookNum <= OT_BOOK_COUNT) "OT" else "NT",
            chapters = chapters.mapValues { it.value.toList() }
        )
    }

    /**
     * Validates a parse result.
     */
    fun validate(result: ParseResult): ValidationResult {
        val errors = mutableListOf<String>()

        if (result.books.isEmpty()) {
            errors.add("No books found in USFM input")
        }

        result.books.forEach { book ->
            if (book.chapters.isEmpty()) {
                errors.add("Book ${book.name} has no chapters")
            }
            book.chapters.forEach { (ch, verses) ->
                if (verses.isEmpty()) {
                    errors.add("${book.name} chapter $ch has no verses")
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.valid() else ValidationResult.invalid(errors)
    }

    private const val OT_BOOK_COUNT = 39

    @Suppress("CyclomaticComplexMethod", "MagicNumber", "LongMethod")
    internal fun usfmBookToNumber(id: String): Int = when (id.uppercase().take(3)) {
        "GEN" -> 1
        "EXO" -> 2
        "LEV" -> 3
        "NUM" -> 4
        "DEU" -> 5
        "JOS" -> 6
        "JDG" -> 7
        "RUT" -> 8
        "1SA" -> 9
        "2SA" -> 10
        "1KI" -> 11
        "2KI" -> 12
        "1CH" -> 13
        "2CH" -> 14
        "EZR" -> 15
        "NEH" -> 16
        "EST" -> 17
        "JOB" -> 18
        "PSA" -> 19
        "PRO" -> 20
        "ECC" -> 21
        "SNG" -> 22
        "ISA" -> 23
        "JER" -> 24
        "LAM" -> 25
        "EZK" -> 26
        "DAN" -> 27
        "HOS" -> 28
        "JOL" -> 29
        "AMO" -> 30
        "OBA" -> 31
        "JON" -> 32
        "MIC" -> 33
        "NAM" -> 34
        "HAB" -> 35
        "ZEP" -> 36
        "HAG" -> 37
        "ZEC" -> 38
        "MAL" -> 39
        "MAT" -> 40
        "MRK" -> 41
        "LUK" -> 42
        "JHN" -> 43
        "ACT" -> 44
        "ROM" -> 45
        "1CO" -> 46
        "2CO" -> 47
        "GAL" -> 48
        "EPH" -> 49
        "PHP" -> 50
        "COL" -> 51
        "1TH" -> 52
        "2TH" -> 53
        "1TI" -> 54
        "2TI" -> 55
        "TIT" -> 56
        "PHM" -> 57
        "HEB" -> 58
        "JAS" -> 59
        "1PE" -> 60
        "2PE" -> 61
        "1JN" -> 62
        "2JN" -> 63
        "3JN" -> 64
        "JUD" -> 65
        "REV" -> 66
        else -> 0
    }

    @Suppress("CyclomaticComplexMethod", "MagicNumber", "LongMethod")
    private fun usfmNumberToName(num: Int): String = when (num) {
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
