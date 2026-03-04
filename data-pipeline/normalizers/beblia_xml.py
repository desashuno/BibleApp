"""
beblia_xml.py — Normalizes Bible text from Beblia/Holy-Bible-XML-Format XML files.

Source: https://github.com/Beblia/Holy-Bible-XML-Format
Only processes files with Public Domain or free license (CC BY-SA, etc.)

XML format:
    <bible translation="..." status="Public Domain">
      <testament name="Old|New">
        <book number="1..66">
          <chapter number="N">
            <verse number="N">text</verse>
          </chapter>
        </book>
      </testament>
    </bible>

Output tables: bibles, books, chapters, verses
"""

from __future__ import annotations

import re
import sqlite3
import xml.etree.ElementTree as ET
from pathlib import Path

# Map from Beblia local filename → (abbreviation, display_name, language, direction)
# Kept in sync with BEBLIA_FREE_BIBLES in download.py
BEBLIA_TRANSLATIONS = {
    # Spanish
    "beblia-SpanishRVR1960.xml": ("RVR1960", "Reina-Valera 1960", "es", "ltr"),
    "beblia-SpanishRV2020.xml": ("RV2020", "Reina-Valera 2020", "es", "ltr"),
    "beblia-SpanishRVES.xml": ("RVES", "Reina-Valera Española", "es", "ltr"),
    "beblia-SpanishVBL2022.xml": ("VBL", "Versión Biblia Libre 2022", "es", "ltr"),
    "beblia-SpanishRV1909.xml": ("RV1909", "Reina-Valera 1909", "es", "ltr"),
    "beblia-Spanish1569.xml": ("SE1569", "Sagradas Escrituras 1569", "es", "ltr"),
    # English
    "beblia-EnglishKJ.xml": ("KJB", "King James Bible (Beblia)", "en", "ltr"),
    # Classical
    "beblia-Latin.xml": ("VULG", "Biblia Sacra Vulgata", "la", "ltr"),
    "beblia-Greek.xml": ("GRK", "Greek New Testament", "el", "ltr"),
    "beblia-Hebrew.xml": ("HEB", "Hebrew Old Testament", "he", "rtl"),
    # European
    "beblia-French.xml": ("LSG", "Louis Segond 1910", "fr", "ltr"),
    "beblia-German.xml": ("DELUT", "Luther Bibel", "de", "ltr"),
    "beblia-German1545.xml": ("LUT1545", "Luther 1545", "de", "ltr"),
    "beblia-Portuguese.xml": ("ARC", "Almeida Revista e Corrigida", "pt", "ltr"),
    "beblia-Italian.xml": ("ITAL", "Diodati / Riveduta", "it", "ltr"),
    "beblia-Dutch.xml": ("SVV", "Statenvertaling", "nl", "ltr"),
    "beblia-Russian.xml": ("RUSV", "Синодальный перевод", "ru", "ltr"),
    "beblia-Ukrainian.xml": ("UKRV", "Українська Біблія", "uk", "ltr"),
    "beblia-Swedish.xml": ("SV1917", "Svenska 1917", "sv", "ltr"),
    "beblia-Danish1819.xml": ("DA1819", "Dansk 1819", "da", "ltr"),
    "beblia-Finnish.xml": ("FI1938", "Raamattu 1938", "fi", "ltr"),
    "beblia-Czech.xml": ("CZBKR", "Bible Kralická", "cs", "ltr"),
    "beblia-Hungarian.xml": ("HUNK", "Károli Gáspár", "hu", "ltr"),
    "beblia-Bosnian.xml": ("BOSN", "Bosanska Biblija", "bs", "ltr"),
    "beblia-Bulgarian2015.xml": ("BG2015", "Библия 2015", "bg", "ltr"),
    "beblia-Albanian1872.xml": ("ALB", "Bibla Shqip 1872", "sq", "ltr"),
    # Asian
    "beblia-ChineseSimplified.xml": ("CNVS", "中文圣经 (简体)", "zh-Hans", "ltr"),
    "beblia-ChineseTraditional.xml": ("CNVT", "中文聖經 (繁體)", "zh-Hant", "ltr"),
    "beblia-Japanese.xml": ("JA", "口語訳聖書", "ja", "ltr"),
    "beblia-Korean.xml": ("KO", "개역한글", "ko", "ltr"),
    "beblia-Hindi.xml": ("HINDI", "हिन्दी बाइबिल", "hi", "ltr"),
    "beblia-Indonesian.xml": ("TB", "Terjemahan Baru", "id", "ltr"),
    "beblia-ThaiSimplified.xml": ("THAI", "พระคัมภีร์ไทย", "th", "ltr"),
    "beblia-Burmese.xml": ("MYAN", "မြန်မာဘာသာ", "my", "ltr"),
    "beblia-Tagalog.xml": ("TAG", "Ang Biblia", "tl", "ltr"),
    # Middle East / Africa
    "beblia-Arabic1978.xml": ("AR1978", "الكتاب المقدس", "ar", "rtl"),
    "beblia-Amharic2000.xml": ("AMH", "መጽሐፍ ቅዱስ", "am", "ltr"),
    "beblia-Swahili.xml": ("SWA", "Biblia Takatifu", "sw", "ltr"),
    "beblia-Armenian1853.xml": ("ARM", "Աստdelays 1853", "hy", "ltr"),
}

# Book names in Spanish for Spanish translations
BOOKS_ES = [
    (1, "Génesis", "OT"), (2, "Éxodo", "OT"), (3, "Levítico", "OT"),
    (4, "Números", "OT"), (5, "Deuteronomio", "OT"), (6, "Josué", "OT"),
    (7, "Jueces", "OT"), (8, "Rut", "OT"), (9, "1 Samuel", "OT"),
    (10, "2 Samuel", "OT"), (11, "1 Reyes", "OT"), (12, "2 Reyes", "OT"),
    (13, "1 Crónicas", "OT"), (14, "2 Crónicas", "OT"), (15, "Esdras", "OT"),
    (16, "Nehemías", "OT"), (17, "Ester", "OT"), (18, "Job", "OT"),
    (19, "Salmos", "OT"), (20, "Proverbios", "OT"), (21, "Eclesiastés", "OT"),
    (22, "Cantares", "OT"), (23, "Isaías", "OT"), (24, "Jeremías", "OT"),
    (25, "Lamentaciones", "OT"), (26, "Ezequiel", "OT"), (27, "Daniel", "OT"),
    (28, "Oseas", "OT"), (29, "Joel", "OT"), (30, "Amós", "OT"),
    (31, "Abdías", "OT"), (32, "Jonás", "OT"), (33, "Miqueas", "OT"),
    (34, "Nahúm", "OT"), (35, "Habacuc", "OT"), (36, "Sofonías", "OT"),
    (37, "Hageo", "OT"), (38, "Zacarías", "OT"), (39, "Malaquías", "OT"),
    (40, "Mateo", "NT"), (41, "Marcos", "NT"), (42, "Lucas", "NT"),
    (43, "Juan", "NT"), (44, "Hechos", "NT"), (45, "Romanos", "NT"),
    (46, "1 Corintios", "NT"), (47, "2 Corintios", "NT"), (48, "Gálatas", "NT"),
    (49, "Efesios", "NT"), (50, "Filipenses", "NT"), (51, "Colosenses", "NT"),
    (52, "1 Tesalonicenses", "NT"), (53, "2 Tesalonicenses", "NT"), (54, "1 Timoteo", "NT"),
    (55, "2 Timoteo", "NT"), (56, "Tito", "NT"), (57, "Filemón", "NT"),
    (58, "Hebreos", "NT"), (59, "Santiago", "NT"), (60, "1 Pedro", "NT"),
    (61, "2 Pedro", "NT"), (62, "1 Juan", "NT"), (63, "2 Juan", "NT"),
    (64, "3 Juan", "NT"), (65, "Judas", "NT"), (66, "Apocalipsis", "NT"),
]

# English book names (used as fallback for non-Spanish translations)
BOOKS_EN = [
    (1, "Genesis", "OT"), (2, "Exodus", "OT"), (3, "Leviticus", "OT"),
    (4, "Numbers", "OT"), (5, "Deuteronomy", "OT"), (6, "Joshua", "OT"),
    (7, "Judges", "OT"), (8, "Ruth", "OT"), (9, "1 Samuel", "OT"),
    (10, "2 Samuel", "OT"), (11, "1 Kings", "OT"), (12, "2 Kings", "OT"),
    (13, "1 Chronicles", "OT"), (14, "2 Chronicles", "OT"), (15, "Ezra", "OT"),
    (16, "Nehemiah", "OT"), (17, "Esther", "OT"), (18, "Job", "OT"),
    (19, "Psalms", "OT"), (20, "Proverbs", "OT"), (21, "Ecclesiastes", "OT"),
    (22, "Song of Solomon", "OT"), (23, "Isaiah", "OT"), (24, "Jeremiah", "OT"),
    (25, "Lamentations", "OT"), (26, "Ezekiel", "OT"), (27, "Daniel", "OT"),
    (28, "Hosea", "OT"), (29, "Joel", "OT"), (30, "Amos", "OT"),
    (31, "Obadiah", "OT"), (32, "Jonah", "OT"), (33, "Micah", "OT"),
    (34, "Nahum", "OT"), (35, "Habakkuk", "OT"), (36, "Zephaniah", "OT"),
    (37, "Haggai", "OT"), (38, "Zechariah", "OT"), (39, "Malachi", "OT"),
    (40, "Matthew", "NT"), (41, "Mark", "NT"), (42, "Luke", "NT"),
    (43, "John", "NT"), (44, "Acts", "NT"), (45, "Romans", "NT"),
    (46, "1 Corinthians", "NT"), (47, "2 Corinthians", "NT"), (48, "Galatians", "NT"),
    (49, "Ephesians", "NT"), (50, "Philippians", "NT"), (51, "Colossians", "NT"),
    (52, "1 Thessalonians", "NT"), (53, "2 Thessalonians", "NT"), (54, "1 Timothy", "NT"),
    (55, "2 Timothy", "NT"), (56, "Titus", "NT"), (57, "Philemon", "NT"),
    (58, "Hebrews", "NT"), (59, "James", "NT"), (60, "1 Peter", "NT"),
    (61, "2 Peter", "NT"), (62, "1 John", "NT"), (63, "2 John", "NT"),
    (64, "3 John", "NT"), (65, "Jude", "NT"), (66, "Revelation", "NT"),
]


def _get_books_for_language(lang: str) -> list[tuple[int, str, str]]:
    """Return the appropriate book-name list for a language."""
    if lang == "es":
        return BOOKS_ES
    return BOOKS_EN


def compute_global_verse_id(book_number: int, chapter: int, verse: int) -> int:
    """Compute BBCCCVVV global verse ID."""
    return book_number * 1_000_000 + chapter * 1_000 + verse


GENERIC_TAG_RE = re.compile(r"<[^>]+>")


def _normalize_red_letter_markup(raw_html: str) -> tuple[str, str | None]:
    """Normalize XML red-letter tags to canonical `<wj>...</wj>` and return plain/html pair."""
    plain = GENERIC_TAG_RE.sub("", raw_html).strip()
    has_wj = "<wj" in raw_html.lower()
    return plain, raw_html if has_wj else None


def _extract_verse_text_and_html(verse: ET.Element) -> tuple[str, str | None]:
    """Extract plain text and canonical html_text for a verse element (best effort)."""
    if len(list(verse)) == 0:
        raw = (verse.text or "").strip()
        return raw, None

    html_parts: list[str] = []
    if verse.text:
        html_parts.append(verse.text)

    for child in verse:
        tag = child.tag.lower()
        inner = "".join(child.itertext()).strip()
        if not inner:
            if child.tail:
                html_parts.append(child.tail)
            continue

        is_red = (
            tag == "fr" or
            tag == "wj" or
            (tag == "q" and child.attrib.get("who", "").lower() == "jesus")
        )

        if is_red:
            html_parts.append(f"<wj>{inner}</wj>")
        else:
            html_parts.append(inner)

        if child.tail:
            html_parts.append(child.tail)

    normalized = "".join(html_parts).strip()
    if not normalized:
        normalized = "".join(verse.itertext()).strip()

    return _normalize_red_letter_markup(normalized)


def _parse_beblia_xml(xml_path: Path) -> list[tuple[int, int, int, str, str | None]]:
    """Parse a Beblia XML file and return list of (book, chapter, verse, text, html_text) tuples."""
    tree = ET.parse(xml_path)
    root = tree.getroot()

    verses: list[tuple[int, int, int, str, str | None]] = []

    for testament in root.findall("testament"):
        for book in testament.findall("book"):
            book_num = int(book.get("number", "0"))
            if book_num < 1 or book_num > 66:
                continue

            for chapter in book.findall("chapter"):
                chap_num = int(chapter.get("number", "0"))
                if chap_num < 1:
                    continue

                for verse in chapter.findall("verse"):
                    verse_num = int(verse.get("number", "0"))
                    if verse_num < 1:
                        continue

                    text, html_text = _extract_verse_text_and_html(verse)
                    if text:
                        verses.append((book_num, chap_num, verse_num, text, html_text))

    return verses


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Read Beblia XML files from raw/bibles/ and write normalized Bible text."""
    bibles_dir = raw_dir / "bibles"

    for filename, (abbr, name, lang, direction) in BEBLIA_TRANSLATIONS.items():
        xml_path = bibles_dir / filename
        if not xml_path.exists():
            print(f"    ⚠ {filename} not found, skipping {abbr}")
            continue

        # Check if this translation already exists (avoid duplicates)
        existing = db.execute(
            "SELECT id FROM bibles WHERE abbreviation = ?", (abbr,)
        ).fetchone()
        if existing:
            print(f"    ✓ {abbr} already imported, skipping")
            continue

        print(f"    → Importing {abbr} ({name}) from Beblia XML")

        # Parse the XML
        rows = _parse_beblia_xml(xml_path)
        if not rows:
            print(f"    ⚠ No verses found in {filename}, skipping")
            continue

        # Use language-specific book names
        books = _get_books_for_language(lang)

        # Insert Bible metadata
        cursor = db.execute(
            "INSERT INTO bibles (abbreviation, name, language, text_direction) VALUES (?, ?, ?, ?)",
            (abbr, name, lang, direction),
        )
        bible_id = cursor.lastrowid

        # Insert books
        book_id_map: dict[int, int] = {}
        for book_number, book_name, testament in books:
            cur = db.execute(
                "INSERT INTO books (bible_id, book_number, name, testament) VALUES (?, ?, ?, ?)",
                (bible_id, book_number, book_name, testament),
            )
            book_id_map[book_number] = cur.lastrowid

        # Count verses per chapter
        chapter_verse_counts: dict[tuple[int, int], int] = {}
        for book_num, chap_num, _, _, _ in rows:
            key = (book_num, chap_num)
            chapter_verse_counts[key] = chapter_verse_counts.get(key, 0) + 1

        # Insert chapters
        chapter_id_map: dict[tuple[int, int], int] = {}
        for (book_num, chap_num) in sorted(chapter_verse_counts.keys()):
            if book_num not in book_id_map:
                continue
            vc = chapter_verse_counts[(book_num, chap_num)]
            cur = db.execute(
                "INSERT INTO chapters (book_id, chapter_number, verse_count) VALUES (?, ?, ?)",
                (book_id_map[book_num], chap_num, vc),
            )
            chapter_id_map[(book_num, chap_num)] = cur.lastrowid

        # Insert verses
        red_letter_found = False
        for book_num, chap_num, verse_num, text, html_text in rows:
            key = (book_num, chap_num)
            if key not in chapter_id_map:
                continue

            global_id = compute_global_verse_id(book_num, chap_num, verse_num)
            if html_text is not None:
                red_letter_found = True
            db.execute(
                "INSERT INTO verses (chapter_id, global_verse_id, verse_number, text, html_text) VALUES (?, ?, ?, ?, ?)",
                (chapter_id_map[key], global_id, verse_num, text, html_text),
            )

        db.commit()
        print(f"      ✓ {abbr}: {len(rows):,} verses imported from Beblia XML")
        if not red_letter_found:
            # TODO(red-letter): many Beblia files have no explicit words-of-Jesus tags.
            print(f"      ⚠ {abbr}: no red-letter markup found in XML; html_text left NULL")
