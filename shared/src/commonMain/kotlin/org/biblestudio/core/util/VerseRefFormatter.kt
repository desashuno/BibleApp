package org.biblestudio.core.util

/**
 * Converts a BBCCCVVV global verse ID into human-readable references.
 *
 * Encoding: `book * 1_000_000 + chapter * 1_000 + verse`
 * Example: Genesis 1:1 = 1_001_001, Revelation 22:21 = 66_022_021
 */
object VerseRefFormatter {

    private val BOOK_NAMES = arrayOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
        "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
        "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
        "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
        "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
        "Zephaniah", "Haggai", "Zechariah", "Malachi",
        "Matthew", "Mark", "Luke", "John", "Acts",
        "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
        "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
        "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
        "1 Peter", "2 Peter", "1 John", "2 John", "3 John",
        "Jude", "Revelation"
    )

    private val BOOK_ABBREVS = arrayOf(
        "Gen", "Exod", "Lev", "Num", "Deut",
        "Josh", "Judg", "Ruth", "1 Sam", "2 Sam",
        "1 Kgs", "2 Kgs", "1 Chr", "2 Chr", "Ezra",
        "Neh", "Esth", "Job", "Ps", "Prov",
        "Eccl", "Song", "Isa", "Jer", "Lam",
        "Ezek", "Dan", "Hos", "Joel", "Amos",
        "Obad", "Jonah", "Mic", "Nah", "Hab",
        "Zeph", "Hag", "Zech", "Mal",
        "Matt", "Mark", "Luke", "John", "Acts",
        "Rom", "1 Cor", "2 Cor", "Gal", "Eph",
        "Phil", "Col", "1 Thess", "2 Thess", "1 Tim",
        "2 Tim", "Titus", "Phlm", "Heb", "Jas",
        "1 Pet", "2 Pet", "1 John", "2 John", "3 John",
        "Jude", "Rev"
    )

    /** Extracts the book number (1-66) from a global verse ID. */
    fun book(globalVerseId: Long): Int = (globalVerseId / 1_000_000).toInt()

    /** Extracts the chapter number from a global verse ID. */
    fun chapter(globalVerseId: Long): Int = ((globalVerseId % 1_000_000) / 1_000).toInt()

    /** Extracts the verse number from a global verse ID. */
    fun verse(globalVerseId: Long): Int = (globalVerseId % 1_000).toInt()

    /** Returns the full book name for a book number (1-66). */
    fun bookName(bookNumber: Int): String = BOOK_NAMES.getOrElse(bookNumber - 1) { "Book $bookNumber" }

    /** Returns the abbreviated book name for a book number (1-66). */
    fun bookAbbrev(bookNumber: Int): String = BOOK_ABBREVS.getOrElse(bookNumber - 1) { "Bk $bookNumber" }

    /**
     * Formats a global verse ID as "Genesis 1:1".
     * Returns "Unknown" for invalid IDs.
     */
    fun format(globalVerseId: Long): String {
        if (globalVerseId <= 0) return "Unknown"
        val b = book(globalVerseId)
        val c = chapter(globalVerseId)
        val v = verse(globalVerseId)
        return "${bookName(b)} $c:$v"
    }

    /**
     * Formats a global verse ID as "Gen 1:1".
     */
    fun formatShort(globalVerseId: Long): String {
        if (globalVerseId <= 0) return "?"
        val b = book(globalVerseId)
        val c = chapter(globalVerseId)
        val v = verse(globalVerseId)
        return "${bookAbbrev(b)} $c:$v"
    }

    /**
     * Formats a verse range as "Genesis 1:1-5" or "Genesis 1:1 - 2:3".
     */
    fun formatRange(startVerseId: Long, endVerseId: Long): String {
        if (startVerseId <= 0) return "Unknown"
        if (endVerseId <= 0 || endVerseId == startVerseId) return format(startVerseId)

        val sb = book(startVerseId)
        val sc = chapter(startVerseId)
        val sv = verse(startVerseId)
        val eb = book(endVerseId)
        val ec = chapter(endVerseId)
        val ev = verse(endVerseId)

        return when {
            sb != eb -> "${format(startVerseId)} - ${format(endVerseId)}"
            sc != ec -> "${bookName(sb)} $sc:$sv - $ec:$ev"
            else -> "${bookName(sb)} $sc:$sv-$ev"
        }
    }
}
