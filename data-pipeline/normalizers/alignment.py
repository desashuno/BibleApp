"""
alignment.py — Parses unfoldingWord Aligned Literal Text (ULT) USFM files to extract
word-level alignment data between Hebrew/Greek originals and English translations.

Populates the alignment_words table with Strong's number ↔ English token mappings.

License attribution (required by CC BY-SA 4.0):
  unfoldingWord® Literal Text (ULT), © unfoldingWord
  Licensed under CC BY-SA 4.0
  https://unfoldingword.org/ult
"""

from __future__ import annotations

import re
import sqlite3
from pathlib import Path

# ---------------------------------------------------------------------------
# USFM book codes → canonical Protestant book numbers (1-based)
# ---------------------------------------------------------------------------

BOOK_CODE_TO_NUMBER: dict[str, int] = {
    "GEN": 1,  "EXO": 2,  "LEV": 3,  "NUM": 4,  "DEU": 5,
    "JOS": 6,  "JDG": 7,  "RUT": 8,  "1SA": 9,  "2SA": 10,
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

# ---------------------------------------------------------------------------
# Compiled regex patterns for USFM markers
# ---------------------------------------------------------------------------

# \id GEN ...
_ID_RE = re.compile(r"\\id\s+(\w+)")
# \c 5
_CHAPTER_RE = re.compile(r"\\c\s+(\d+)")
# \v 3
_VERSE_RE = re.compile(r"\\v\s+(\d+)")
# \zaln-s |x-strong="H7225" x-lemma="..." x-occurrence="1" ...\*
# Captures the full attribute string between | and \*
_ZALN_START_RE = re.compile(r"\\zaln-s\s*\|([^*\\]+)\\\*")
# \zaln-e\*
_ZALN_END_RE = re.compile(r"\\zaln-e\\\*")
# \w word|x-occurrence="1" x-occurrences="1"\w*  (attributes optional)
_WORD_RE = re.compile(r"\\w\s+([^|\\]+?)(?:\|[^\\]*)?\s*\\w\*")
# x-strong="H7225" inside zaln-s attributes
_STRONG_ATTR_RE = re.compile(r'x-strong="([^"]+)"')

# ---------------------------------------------------------------------------
# USFM parser
# ---------------------------------------------------------------------------

def _parse_usfm_book(filepath: Path) -> list[dict]:
    """
    Parse one ULT USFM file and return a list of alignment row dicts.

    Each dict has keys: global_verse_id, english_position, english_token,
    original_position, strongs_number.

    Uses an event-based scan: collects all marker positions, sorts them,
    then processes in document order.
    """
    text = filepath.read_text(encoding="utf-8", errors="replace")

    # Collect all events: (position, type, value)
    events: list[tuple[int, str, object]] = []

    for m in _ID_RE.finditer(text):
        events.append((m.start(), "id", m.group(1)[:3].upper()))

    for m in _CHAPTER_RE.finditer(text):
        events.append((m.start(), "chapter", int(m.group(1))))

    for m in _VERSE_RE.finditer(text):
        events.append((m.start(), "verse", int(m.group(1))))

    for m in _ZALN_START_RE.finditer(text):
        attr_string = m.group(1)
        sm = _STRONG_ATTR_RE.search(attr_string)
        if sm:
            events.append((m.start(), "zaln_start", sm.group(1)))

    for m in _ZALN_END_RE.finditer(text):
        events.append((m.start(), "zaln_end", None))

    for m in _WORD_RE.finditer(text):
        token = m.group(1).strip()
        if token:
            events.append((m.start(), "word", token))

    events.sort(key=lambda e: e[0])

    # Process events in document order
    book_number: int | None = None
    chapter = 0
    verse = 0
    strongs_stack: list[tuple[str, int]] = []  # (strongs_number, original_position)
    english_pos = 0
    original_pos = 0
    rows: list[dict] = []

    for _, event_type, value in events:
        if event_type == "id":
            book_number = BOOK_CODE_TO_NUMBER.get(str(value))
        elif event_type == "chapter":
            chapter = int(value)  # type: ignore[arg-type]
            verse = 0
        elif event_type == "verse":
            verse = int(value)  # type: ignore[arg-type]
            english_pos = 0
            original_pos = 0
            strongs_stack = []
        elif event_type == "zaln_start":
            original_pos += 1
            strongs_stack.append((str(value), original_pos))
        elif event_type == "zaln_end":
            if strongs_stack:
                strongs_stack.pop()
        elif event_type == "word":
            if strongs_stack and book_number is not None and verse > 0:
                english_pos += 1
                global_verse_id = book_number * 1_000_000 + chapter * 1_000 + verse
                strongs_number, orig_pos = strongs_stack[-1]
                rows.append({
                    "global_verse_id": global_verse_id,
                    "english_position": english_pos,
                    "english_token": str(value),
                    "original_position": orig_pos,
                    "strongs_number": strongs_number,
                })

    return rows


# ---------------------------------------------------------------------------
# Public entry point
# ---------------------------------------------------------------------------

_INSERT_SQL = """
    INSERT INTO alignment_words
        (global_verse_id, english_position, english_token, original_position, strongs_number)
    VALUES
        (:global_verse_id, :english_position, :english_token, :original_position, :strongs_number)
"""

_BATCH_SIZE = 10_000


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Populate alignment_words from unfoldingWord ULT USFM files."""
    ult_dir = raw_dir / "alignment"

    if not ult_dir.exists():
        print(f"  ⚠ ULT directory not found: {ult_dir}")
        print("    Run: python download.py --only alignment")
        return

    usfm_files = sorted(ult_dir.glob("*.usfm"))
    if not usfm_files:
        print(f"  ⚠ No .usfm files found in {ult_dir}")
        return

    print(f"  Processing {len(usfm_files)} USFM book files...")

    total_rows = 0
    batch: list[dict] = []

    for usfm_file in usfm_files:
        try:
            rows = _parse_usfm_book(usfm_file)
            batch.extend(rows)

            if len(batch) >= _BATCH_SIZE:
                db.executemany(_INSERT_SQL, batch)
                db.commit()
                total_rows += len(batch)
                batch = []
        except Exception as exc:
            print(f"    ⚠ Failed to parse {usfm_file.name}: {exc}")
            continue

    if batch:
        db.executemany(_INSERT_SQL, batch)
        db.commit()
        total_rows += len(batch)

    print(f"  ✓ Inserted {total_rows:,} alignment_words entries")
    print("  Attribution: unfoldingWord® Literal Text (ULT), CC BY-SA 4.0 — https://unfoldingword.org/ult")
