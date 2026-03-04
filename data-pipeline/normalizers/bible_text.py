"""
bible_text.py — Normalizes Bible text into bibles, books, chapters, verses tables.

Sources:
  - scrollmapper/bible_databases SQLite (raw/bibles/scrollmapper-bible.db)

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

# Known translations in scrollmapper with their metadata
TRANSLATIONS = {
    "t_kjv": ("KJV", "King James Version", "en", "ltr"),
    "t_asv": ("ASV", "American Standard Version", "en", "ltr"),
    "t_web": ("WEB", "World English Bible", "en", "ltr"),
    "t_ylt": ("YLT", "Young's Literal Translation", "en", "ltr"),
    "t_bbe": ("BBE", "Bible in Basic English", "en", "ltr"),
}


def compute_global_verse_id(book_number: int, chapter: int, verse: int) -> int:
    """Compute BBCCCVVV global verse ID."""
    return book_number * 1_000_000 + chapter * 1_000 + verse


USFM_WJ_RE = re.compile(r"\\\\wj\s+(.*?)\\\\wj\*", re.IGNORECASE | re.DOTALL)
OSIS_JESUS_Q_RE = re.compile(r"<q\b[^>]*who\s*=\s*['\"]Jesus['\"][^>]*>(.*?)</q>", re.IGNORECASE | re.DOTALL)
GENERIC_TAG_RE = re.compile(r"<[^>]+>")


def normalize_red_letter_text(raw_text: str) -> tuple[str, str | None]:
    """Return (plain_text, html_text) with canonical `<wj>...</wj>` when source markup exists."""
    html_text = raw_text
    html_text = OSIS_JESUS_Q_RE.sub(r"<wj>\1</wj>", html_text)
    html_text = USFM_WJ_RE.sub(lambda m: f"<wj>{m.group(1).strip()}</wj>", html_text)

    has_wj = "<wj" in html_text.lower()
    plain_text = GENERIC_TAG_RE.sub("", html_text).strip()
    if not plain_text:
        plain_text = raw_text.strip()

    return plain_text, html_text if has_wj else None


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Read scrollmapper SQLite and write normalized Bible text."""
    source_db_path = raw_dir / "bibles" / "scrollmapper-bible.db"

    if not source_db_path.exists():
        print("    ⚠ scrollmapper-bible.db not found, skipping Bible text")
        return

    # Check if WEB USFM is available (preferred source with red-letter)
    web_usfm_dir = raw_dir / "bibles" / "web-usfm"
    skip_web = web_usfm_dir.exists() and any(web_usfm_dir.glob("*.usfm"))
    if skip_web:
        print("    ℹ Skipping scrollmapper WEB (t_web) — using USFM source with red-letter instead")

    source = sqlite3.connect(str(source_db_path))
    source.row_factory = sqlite3.Row

    # Discover which translation tables exist in scrollmapper DB
    # Schema (2024 branch): tables named t_kjv, t_asv, … with columns:
    #   id INTEGER (BBCCCVVV), b INTEGER (book), c INTEGER (chapter),
    #   v INTEGER (verse), t TEXT (text)
    # Metadata in bible_version_key: table, abbreviation, version, language, …
    tables = {
        row[0]
        for row in source.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        ).fetchall()
    }

    # Read version metadata from bible_version_key if available
    version_meta: dict[str, tuple[str, str, str, str]] = {}
    if "bible_version_key" in tables:
        for row in source.execute(
            'SELECT "table", abbreviation, version, language FROM bible_version_key'
        ).fetchall():
            tbl, abbr, full_name, lang = row
            version_meta[tbl] = (abbr, full_name, lang or "en", "ltr")

    # Fall back to hardcoded metadata for tables not in bible_version_key
    for tbl, meta in TRANSLATIONS.items():
        if tbl not in version_meta and tbl in tables:
            version_meta[tbl] = meta

    # Import each translation
    for table_name, (abbr, name, lang, direction) in version_meta.items():
        if table_name not in tables:
            continue

        # Skip WEB from scrollmapper if USFM source is available
        if skip_web and table_name == "t_web":
            continue

        red_letter_found = False

        # Verify the table has the expected columns (b, c, v, t)
        try:
            cols = {r[1] for r in source.execute(f"PRAGMA table_info({table_name})").fetchall()}
        except Exception:
            continue

        if not {"b", "c", "v", "t"}.issubset(cols):
            continue

        print(f"    → Importing {abbr} ({name})")

        # Insert Bible metadata
        cursor = db.execute(
            "INSERT INTO bibles (abbreviation, name, language, text_direction) VALUES (?, ?, ?, ?)",
            (abbr, name, lang, direction),
        )
        bible_id = cursor.lastrowid

        # Insert books for this Bible
        book_id_map = {}  # book_number -> db book id
        for book_number, book_name, testament in BOOKS:
            cur = db.execute(
                "INSERT INTO books (bible_id, book_number, name, testament) VALUES (?, ?, ?, ?)",
                (bible_id, book_number, book_name, testament),
            )
            book_id_map[book_number] = cur.lastrowid

        # Read all verses from source (columns: b=book, c=chapter, v=verse, t=text)
        try:
            rows = source.execute(
                f"SELECT b, c, v, t FROM {table_name} ORDER BY b, c, v"
            ).fetchall()
        except Exception as e:
            print(f"    ⚠ Error reading {table_name}: {e}")
            continue

        # Group by (book, chapter) to build chapters and verses
        chapter_id_map = {}  # (book_number, chapter_number) -> chapter_id
        chapter_verse_counts = {}  # (book_number, chapter_number) -> count

        # First pass: count verses per chapter
        for row in rows:
            book_num = int(row[0])
            chapter_num = int(row[1])
            key = (book_num, chapter_num)
            chapter_verse_counts[key] = chapter_verse_counts.get(key, 0) + 1

        # Insert chapters
        for (book_num, chapter_num) in sorted(chapter_verse_counts.keys()):
            if book_num not in book_id_map:
                continue
            vc = chapter_verse_counts[(book_num, chapter_num)]
            cur = db.execute(
                "INSERT INTO chapters (book_id, chapter_number, verse_count) VALUES (?, ?, ?)",
                (book_id_map[book_num], chapter_num, vc),
            )
            chapter_id_map[(book_num, chapter_num)] = cur.lastrowid

        # Insert verses
        for row in rows:
            book_num = int(row[0])
            chapter_num = int(row[1])
            verse_num = int(row[2])
            raw_text = str(row[3])
            text, html_text = normalize_red_letter_text(raw_text)
            if html_text is not None:
                red_letter_found = True

            key = (book_num, chapter_num)
            if key not in chapter_id_map:
                continue

            global_id = compute_global_verse_id(book_num, chapter_num, verse_num)

            db.execute(
                "INSERT INTO verses (chapter_id, global_verse_id, verse_number, text, html_text) VALUES (?, ?, ?, ?, ?)",
                (chapter_id_map[key], global_id, verse_num, text, html_text),
            )

        db.commit()
        print(f"      ✓ {abbr}: {len(rows):,} verses imported")
        if not red_letter_found:
            # TODO(red-letter): scrollmapper sources generally do not ship words-of-Jesus metadata.
            print(f"      ⚠ {abbr}: no red-letter markup found in source; html_text left NULL")

    source.close()
