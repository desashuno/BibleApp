"""
web_usfm.py — Parses World English Bible USFM files from eBible.org to import
Bible text with red-letter (words of Jesus) markup.

Source:
  - eBible.org WEB USFM: https://ebible.org/Scriptures/eng-web_usfm.zip
  - License: Public Domain

The WEB USFM files use \\wj ... \\wj* markers to delimit words of Jesus.
This normalizer produces both plain_text and html_text (with <wj>...</wj> tags).

Output tables: bibles, books, chapters, verses
"""

from __future__ import annotations

import re
import sqlite3
from pathlib import Path

# Book metadata: (book_number, name, testament)
BOOKS = [
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

BOOK_MAP = {num: (name, testament) for num, name, testament in BOOKS}

# USFM book ID → canonical number
USFM_BOOK_ID: dict[str, int] = {
    "GEN": 1, "EXO": 2, "LEV": 3, "NUM": 4, "DEU": 5,
    "JOS": 6, "JDG": 7, "RUT": 8, "1SA": 9, "2SA": 10,
    "1KI": 11, "2KI": 12, "1CH": 13, "2CH": 14, "EZR": 15,
    "NEH": 16, "EST": 17, "JOB": 18, "PSA": 19, "PRO": 20,
    "ECC": 21, "SNG": 22, "ISA": 23, "JER": 24, "LAM": 25,
    "EZK": 26, "DAN": 27, "HOS": 28, "JOL": 29, "AMO": 30,
    "OBA": 31, "JON": 32, "MIC": 33, "NAM": 34, "HAB": 35,
    "ZEP": 36, "HAG": 37, "ZEC": 38, "MAL": 39,
    "MAT": 40, "MRK": 41, "LUK": 42, "JHN": 43, "ACT": 44,
    "ROM": 45, "1CO": 46, "2CO": 47, "GAL": 48, "EPH": 49,
    "PHP": 50, "COL": 51, "1TH": 52, "2TH": 53, "1TI": 54,
    "2TI": 55, "TIT": 56, "PHM": 57, "HEB": 58, "JAS": 59,
    "1PE": 60, "2PE": 61, "1JN": 62, "2JN": 63, "3JN": 64,
    "JUD": 65, "REV": 66,
}

# Regex patterns for USFM markers
_ID_RE = re.compile(r"\\id\s+(\w+)")
_CHAPTER_RE = re.compile(r"\\c\s+(\d+)")
_VERSE_RE = re.compile(r"\\v\s+(\d+)\s*")
_WJ_START_RE = re.compile(r"\\wj\s+", re.IGNORECASE)
_WJ_END_RE = re.compile(r"\\wj\*", re.IGNORECASE)
_WORD_RE = re.compile(r"\\w\s+([^|\\]+?)(?:\|[^\\]*)?\s*\\w\*")
_FOOTNOTE_RE = re.compile(r"\\f\s+.*?\\f\*", re.DOTALL)
_CROSSREF_RE = re.compile(r"\\x\s+.*?\\x\*", re.DOTALL)
_MARKER_RE = re.compile(r"\\(?:p|pi\d?|q\d?|m|mi|nb|li\d?|b|d|sp|s\d?|r|ms\d?|mr|sr)\s*")
_ADD_RE = re.compile(r"\\add\s+(.*?)\\add\*", re.DOTALL)
_ND_RE = re.compile(r"\\nd\s+(.*?)\\nd\*", re.DOTALL)
_GENERIC_MARKER_RE = re.compile(r"\\[a-z]+\d?\s*\*?")


def _parse_usfm_file(filepath: Path) -> list[dict]:
    """
    Parse a single WEB USFM file and return verse records.

    Each record: {book_number, chapter, verse, plain_text, html_text}
    html_text contains <wj>...</wj> for words of Jesus (None if no red letter).
    """
    text = filepath.read_text(encoding="utf-8", errors="replace")

    # Identify book
    id_match = _ID_RE.search(text)
    if not id_match:
        return []
    book_code = id_match.group(1)[:3].upper()
    book_number = USFM_BOOK_ID.get(book_code)
    if book_number is None:
        return []

    # Collect events in document order
    events: list[tuple[int, str, str]] = []

    for m in _CHAPTER_RE.finditer(text):
        events.append((m.start(), "chapter", m.group(1)))

    for m in _VERSE_RE.finditer(text):
        events.append((m.start(), "verse", m.group(1)))

    for m in _WJ_START_RE.finditer(text):
        events.append((m.start(), "wj_start", ""))

    for m in _WJ_END_RE.finditer(text):
        events.append((m.start(), "wj_end", ""))

    # Collect text segments (everything between markers)
    # We need to track position → text mapping, handling footnotes/crossrefs
    # Strip footnotes and crossrefs first
    cleaned = _FOOTNOTE_RE.sub("", text)
    cleaned = _CROSSREF_RE.sub("", cleaned)

    events.sort(key=lambda e: e[0])

    # Process line by line for simpler verse extraction
    verses: list[dict] = []
    chapter = 0
    current_verse = 0
    in_wj = False
    verse_parts: list[str] = []
    verse_html_parts: list[str] = []
    has_wj = False

    def _flush_verse():
        nonlocal current_verse, verse_parts, verse_html_parts, has_wj
        if current_verse > 0 and verse_parts:
            plain = " ".join(verse_parts).strip()
            plain = re.sub(r"\s+", " ", plain)
            if plain:
                html = " ".join(verse_html_parts).strip()
                html = re.sub(r"\s+", " ", html)
                verses.append({
                    "book_number": book_number,
                    "chapter": chapter,
                    "verse": current_verse,
                    "plain_text": plain,
                    "html_text": html if has_wj else None,
                })
        verse_parts = []
        verse_html_parts = []
        has_wj = False

    for line in cleaned.split("\n"):
        line = line.strip()
        if not line:
            continue

        # Chapter marker
        cm = re.match(r"\\c\s+(\d+)", line)
        if cm:
            _flush_verse()
            chapter = int(cm.group(1))
            current_verse = 0
            continue

        # Process verse markers within the line (can be multiple per line)
        # Split line at verse markers
        parts = re.split(r"(\\v\s+\d+\s*)", line)
        for part in parts:
            vm = re.match(r"\\v\s+(\d+)", part)
            if vm:
                _flush_verse()
                current_verse = int(vm.group(1))
                continue

            if current_verse == 0:
                continue

            # Process text content, handling \wj markers
            segment = part
            # Strip paragraph/poetry markers
            segment = _MARKER_RE.sub("", segment)
            # Handle \add (italicized additions)
            segment = _ADD_RE.sub(r"\1", segment)
            # Handle \nd (divine name)
            segment = _ND_RE.sub(r"\1", segment)
            # Handle \w word|attributes\w* (extract just the word)
            segment = _WORD_RE.sub(r"\1", segment)

            # Process wj markers for html
            html_segment = segment
            plain_segment = segment

            # Replace \wj ... \wj* with <wj>...</wj>
            html_segment = _WJ_START_RE.sub("<wj>", html_segment)
            html_segment = _WJ_END_RE.sub("</wj>", html_segment)

            # Track ongoing wj state
            wj_starts = len(re.findall(r"<wj>", html_segment))
            wj_ends = len(re.findall(r"</wj>", html_segment))

            if in_wj and wj_starts == 0 and wj_ends == 0:
                # Continuation of wj from previous segment
                html_segment = "<wj>" + html_segment + "</wj>"

            if wj_starts > 0 or wj_ends > 0 or in_wj:
                has_wj = True

            # Update wj state
            if wj_starts > wj_ends:
                in_wj = True
            elif wj_ends > wj_starts:
                in_wj = False

            # Strip remaining markers for plain text
            plain_segment = re.sub(r"\\wj\s+", "", plain_segment, flags=re.IGNORECASE)
            plain_segment = re.sub(r"\\wj\*", "", plain_segment, flags=re.IGNORECASE)
            plain_segment = _GENERIC_MARKER_RE.sub("", plain_segment)
            html_segment = _GENERIC_MARKER_RE.sub("", html_segment)

            plain_segment = plain_segment.strip()
            html_segment = html_segment.strip()

            if plain_segment:
                verse_parts.append(plain_segment)
            if html_segment:
                verse_html_parts.append(html_segment)

    _flush_verse()
    return verses


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import WEB Bible text from USFM files with red-letter markup."""
    web_dir = raw_dir / "bibles" / "web-usfm"

    if not web_dir.exists():
        print("    ⚠ web-usfm/ not found, skipping WEB USFM import")
        return

    usfm_files = sorted(web_dir.glob("*.usfm"))
    if not usfm_files:
        print("    ⚠ No .usfm files found in web-usfm/")
        return

    print(f"    → Importing WEB from {len(usfm_files)} USFM files (with red-letter)")

    # Insert Bible metadata
    cursor = db.execute(
        "INSERT INTO bibles (abbreviation, name, language, text_direction) VALUES (?, ?, ?, ?)",
        ("WEB", "World English Bible", "en", "ltr"),
    )
    bible_id = cursor.lastrowid

    # Insert books
    book_id_map: dict[int, int] = {}
    for book_number, book_name, testament in BOOKS:
        cur = db.execute(
            "INSERT INTO books (bible_id, book_number, name, testament) VALUES (?, ?, ?, ?)",
            (bible_id, book_number, book_name, testament),
        )
        book_id_map[book_number] = cur.lastrowid

    # Parse all USFM files
    all_verses: list[dict] = []
    for usfm_file in usfm_files:
        try:
            verses = _parse_usfm_file(usfm_file)
            all_verses.extend(verses)
        except Exception as e:
            print(f"      ⚠ Failed to parse {usfm_file.name}: {e}")

    if not all_verses:
        print("    ⚠ No verses parsed from WEB USFM files")
        return

    # Group by (book, chapter) to build chapters
    chapter_verses: dict[tuple[int, int], list[dict]] = {}
    for v in all_verses:
        key = (v["book_number"], v["chapter"])
        chapter_verses.setdefault(key, []).append(v)

    # Insert chapters and verses
    red_letter_count = 0
    total_verses = 0

    for (book_num, chapter_num) in sorted(chapter_verses.keys()):
        if book_num not in book_id_map:
            continue

        vlist = chapter_verses[(book_num, chapter_num)]
        cur = db.execute(
            "INSERT INTO chapters (book_id, chapter_number, verse_count) VALUES (?, ?, ?)",
            (book_id_map[book_num], chapter_num, len(vlist)),
        )
        chapter_id = cur.lastrowid

        for v in vlist:
            global_id = book_num * 1_000_000 + chapter_num * 1_000 + v["verse"]
            db.execute(
                "INSERT INTO verses (chapter_id, global_verse_id, verse_number, text, html_text) "
                "VALUES (?, ?, ?, ?, ?)",
                (chapter_id, global_id, v["verse"], v["plain_text"], v["html_text"]),
            )
            total_verses += 1
            if v["html_text"] is not None:
                red_letter_count += 1

    db.commit()
    print(f"      ✓ WEB: {total_verses:,} verses imported")
    print(f"      ✓ Red-letter verses: {red_letter_count:,}")
