"""
lexicon.py — Normalizes Hebrew & Greek lexicon data into lexicon_entries and word_occurrences.

Sources:
  - STEPBible TBESH (raw/lexicon/TBESH.txt) — Hebrew lexicon (abridged BDB)
  - STEPBible TBESG (raw/lexicon/TBESG.txt) — Greek lexicon

Output tables: lexicon_entries, word_occurrences
"""

from __future__ import annotations

import re
import sqlite3
from pathlib import Path


def _import_step_lexicon(
    file_path: Path,
    db: sqlite3.Connection,
    label: str,
) -> int:
    """
    Import a STEPBible TBESH or TBESG lexicon file.

    These are tab-separated files. Data lines typically contain:
      - Extended Strong's number (e.g., H0001, G0001)
      - Original word (Hebrew or Greek)
      - Transliteration
      - Brief definition
      - Possibly usage notes or additional fields

    Lines starting with '#' or '$' are comments/headers.
    """
    if not file_path.exists():
        print(f"    ⚠ {file_path.name} not found, skipping {label}")
        return 0

    print(f"    → Importing {label} from {file_path.name}")

    count = 0

    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()

            # Skip comments, empty lines, and headers
            if not line or line.startswith("#") or line.startswith("$"):
                continue

            parts = line.split("\t")
            if len(parts) < 3:
                continue

            # First column should be a Strong's number
            strongs = parts[0].strip()
            if not re.match(r"^[HG]\d+", strongs):
                continue

            # Normalize Strong's: keep only the primary number (before any extensions like 'a', 'b')
            strongs_primary = re.match(r"^([HG]\d+)", strongs).group(1)

            # Extract fields
            original_word = parts[1].strip() if len(parts) > 1 else ""
            transliteration = parts[2].strip() if len(parts) > 2 else ""
            definition = parts[3].strip() if len(parts) > 3 else ""
            usage_notes = parts[4].strip() if len(parts) > 4 else None

            # If definition is empty, try to find it in later columns
            if not definition:
                for i in range(3, len(parts)):
                    candidate = parts[i].strip()
                    if candidate and len(candidate) > 5 and not re.match(r"^[HG]\d+", candidate):
                        definition = candidate
                        break

            if not definition:
                continue

            # Use INSERT OR IGNORE to handle duplicate Strong's numbers
            db.execute(
                """INSERT OR IGNORE INTO lexicon_entries
                   (strongs_number, original_word, transliteration, definition, usage_notes)
                   VALUES (?, ?, ?, ?, ?)""",
                (strongs_primary, original_word, transliteration, definition, usage_notes),
            )
            count += 1

            if count % 1_000 == 0:
                db.commit()

    db.commit()
    return count


def build_word_occurrences(db: sqlite3.Connection) -> int:
    """
    Build word_occurrences table from morphology data.

    For each unique (strongs_number, global_verse_id, word_position) in
    the morphology table, create an entry in word_occurrences.
    """
    print("    → Building word_occurrences from morphology data")

    count_row = db.execute("SELECT COUNT(*) FROM morphology").fetchone()
    if not count_row or count_row[0] == 0:
        print("    ⚠ No morphology data available, skipping word_occurrences")
        return 0

    db.execute(
        """INSERT INTO word_occurrences (strongs_number, global_verse_id, word_position)
           SELECT DISTINCT strongs_number, global_verse_id, word_position
           FROM morphology
           WHERE strongs_number != ''
           ORDER BY global_verse_id, word_position"""
    )
    db.commit()

    count = db.execute("SELECT COUNT(*) FROM word_occurrences").fetchone()[0]
    return count


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import Hebrew and Greek lexicon data."""

    # Hebrew lexicon (TBESH)
    count_heb = _import_step_lexicon(
        raw_dir / "lexicon" / "TBESH.txt",
        db,
        "Hebrew lexicon (TBESH)",
    )
    if count_heb:
        print(f"      ✓ Hebrew lexicon: {count_heb:,} entries")

    # Greek lexicon (TBESG)
    count_grk = _import_step_lexicon(
        raw_dir / "lexicon" / "TBESG.txt",
        db,
        "Greek lexicon (TBESG)",
    )
    if count_grk:
        print(f"      ✓ Greek lexicon: {count_grk:,} entries")

    # Build word_occurrences from morphology
    count_occ = build_word_occurrences(db)
    if count_occ:
        print(f"      ✓ Word occurrences: {count_occ:,} entries")
