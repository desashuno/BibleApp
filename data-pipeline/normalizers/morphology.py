"""
morphology.py — Normalizes Hebrew & Greek morphology data into the morphology table.

Sources:
  - STEPBible TAHOT (raw/morphology/TAHOT.txt) — Hebrew OT word-by-word
  - STEPBible TAGNT (raw/morphology/TAGNT.txt) — Greek NT word-by-word

Output table: morphology
"""

from __future__ import annotations

import re
import sqlite3
from pathlib import Path


def compute_global_verse_id(book_number: int, chapter: int, verse: int) -> int:
    """Compute BBCCCVVV global verse ID."""
    return book_number * 1_000_000 + chapter * 1_000 + verse


# STEPBible book abbreviation → canonical book number (1-66)
STEP_BOOK_MAP: dict[str, int] = {
    "Gen": 1, "Exo": 2, "Lev": 3, "Num": 4, "Deu": 5,
    "Jos": 6, "Jdg": 7, "Rut": 8, "1Sa": 9, "2Sa": 10,
    "1Ki": 11, "2Ki": 12, "1Ch": 13, "2Ch": 14, "Ezr": 15,
    "Neh": 16, "Est": 17, "Job": 18, "Psa": 19, "Pro": 20,
    "Ecc": 21, "Sng": 22, "Isa": 23, "Jer": 24, "Lam": 25,
    "Eze": 26, "Dan": 27, "Hos": 28, "Joe": 29, "Amo": 30,
    "Oba": 31, "Jon": 32, "Mic": 33, "Nah": 34, "Hab": 35,
    "Zep": 36, "Hag": 37, "Zec": 38, "Mal": 39,
    "Mat": 40, "Mrk": 41, "Luk": 42, "Jhn": 43, "Act": 44,
    "Rom": 45, "1Co": 46, "2Co": 47, "Gal": 48, "Eph": 49,
    "Php": 50, "Col": 51, "1Th": 52, "2Th": 53, "1Ti": 54,
    "2Ti": 55, "Tit": 56, "Phm": 57, "Heb": 58, "Jas": 59,
    "1Pe": 60, "2Pe": 61, "1Jn": 62, "2Jn": 63, "3Jn": 64,
    "Jud": 65, "Rev": 66,
}

# Verse reference pattern: Book.Chapter.Verse (e.g., "Gen.1.1")
VERSE_REF_PATTERN = re.compile(r"^(\w+)\.(\d+)\.(\d+)")


def parse_verse_ref(ref: str) -> tuple[int, int, int] | None:
    """Parse a STEPBible verse reference like 'Gen.1.1' into (book_num, chapter, verse)."""
    m = VERSE_REF_PATTERN.match(ref)
    if not m:
        return None
    book_abbr = m.group(1)
    book_num = STEP_BOOK_MAP.get(book_abbr)
    if book_num is None:
        return None
    return book_num, int(m.group(2)), int(m.group(3))


def _import_step_morphology(
    file_path: Path,
    db: sqlite3.Connection,
    label: str,
) -> int:
    """
    Import a STEPBible TAHOT or TAGNT file.

    These are tab-separated files where each data line contains:
      - Columns vary but generally include: Reference, Morphology, Strong's, Word, Gloss, etc.
      - Lines starting with '#' or empty lines are comments/headers.
      - The first column with a verse reference (Book.Chapter.Verse) identifies the verse.
    """
    if not file_path.exists():
        print(f"    ⚠ {file_path.name} not found, skipping {label}")
        return 0

    print(f"    → Importing {label} from {file_path.name}")

    count = 0
    current_verse: tuple[int, int, int] | None = None
    word_position = 0

    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()

            # Skip comments and empty lines
            if not line or line.startswith("#") or line.startswith("$"):
                continue

            parts = line.split("\t")
            if len(parts) < 4:
                continue

            # Try to find verse reference in first column
            ref_str = parts[0].strip()
            parsed = parse_verse_ref(ref_str)

            if parsed:
                if parsed != current_verse:
                    current_verse = parsed
                    word_position = 0

                word_position += 1
                book_num, chapter, verse = current_verse
                global_id = compute_global_verse_id(book_num, chapter, verse)

                # Extract fields — exact column positions vary between TAHOT/TAGNT
                # General pattern: ref | type | strongs | morphology | word | gloss
                strongs = ""
                parsing_code = ""
                surface_form = ""
                lemma = ""
                gloss = ""

                for i, part in enumerate(parts[1:], start=1):
                    part = part.strip()
                    # Strong's numbers start with H or G
                    if re.match(r"^[HG]\d+", part) and not strongs:
                        strongs = part
                    # Morphology codes are typically like "V-QAL-I3MS" or "N-MSA"
                    elif re.match(r"^[A-Z][a-zA-Z0-9-]+$", part) and len(part) > 2 and not parsing_code:
                        parsing_code = part
                    # Hebrew/Greek characters (surface form)
                    elif any("\u0590" <= c <= "\u05FF" or "\u0370" <= c <= "\u03FF" for c in part):
                        if not surface_form:
                            surface_form = part
                        elif not lemma:
                            lemma = part
                    # English gloss (typically the last meaningful field)
                    elif part and not any(c in part for c in "=/<>") and i >= len(parts) - 2:
                        gloss = part

                if strongs:
                    db.execute(
                        """INSERT INTO morphology
                           (global_verse_id, word_position, strongs_number, parsing_code, surface_form, lemma, gloss)
                           VALUES (?, ?, ?, ?, ?, ?, ?)""",
                        (global_id, word_position, strongs, parsing_code, surface_form, lemma, gloss),
                    )
                    count += 1

                    if count % 10_000 == 0:
                        db.commit()

    db.commit()
    return count


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import Hebrew and Greek morphology data."""

    # Hebrew OT
    count_heb = _import_step_morphology(
        raw_dir / "morphology" / "TAHOT.txt",
        db,
        "Hebrew OT morphology (TAHOT)",
    )
    if count_heb:
        print(f"      ✓ Hebrew morphology: {count_heb:,} words")

    # Greek NT
    count_grk = _import_step_morphology(
        raw_dir / "morphology" / "TAGNT.txt",
        db,
        "Greek NT morphology (TAGNT)",
    )
    if count_grk:
        print(f"      ✓ Greek morphology: {count_grk:,} words")
