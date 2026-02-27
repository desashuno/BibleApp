package org.biblestudio.features.bible_reader.data.mappers

import migrations.Bibles
import migrations.Books
import migrations.Chapters
import migrations.Verses
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Chapter
import org.biblestudio.features.bible_reader.domain.entities.Verse

// ── Bible ───────────────────────────────────────────────────────────

internal fun Bibles.toBible(): Bible = Bible(
    id = id,
    abbreviation = abbreviation,
    name = name,
    language = language,
    textDirection = text_direction
)

// ── Book ────────────────────────────────────────────────────────────

internal fun Books.toBook(): Book = Book(
    id = id,
    bibleId = bible_id,
    bookNumber = book_number,
    name = name,
    testament = testament
)

// ── Chapter ─────────────────────────────────────────────────────────

internal fun Chapters.toChapter(): Chapter = Chapter(
    id = id,
    bookId = book_id,
    chapterNumber = chapter_number,
    verseCount = verse_count
)

// ── Verse ───────────────────────────────────────────────────────────

internal fun Verses.toVerse(): Verse = Verse(
    id = id,
    chapterId = chapter_id,
    globalVerseId = global_verse_id,
    verseNumber = verse_number,
    text = text,
    htmlText = html_text
)
