"""
cross_references.py — Normalizes cross-reference data into cross_references and parallel_passages.

Sources:
  - OpenBible.info cross-references (raw/cross-references/openbible_cross_references.txt)
  - scrollmapper cross-references  (raw/bibles/cross_references_scrollmapper.txt)

Output tables: cross_references, parallel_passages
"""

from __future__ import annotations

import re
import sqlite3
from pathlib import Path

# Book name/abbreviation → canonical number mapping for OpenBible.info format
# OpenBible uses OSIS-style abbreviations: Gen, Exod, Lev, etc.
OSIS_BOOK_MAP: dict[str, int] = {
    "Gen": 1, "Exod": 2, "Lev": 3, "Num": 4, "Deut": 5,
    "Josh": 6, "Judg": 7, "Ruth": 8, "1Sam": 9, "2Sam": 10,
    "1Kgs": 11, "2Kgs": 12, "1Chr": 13, "2Chr": 14, "Ezra": 15,
    "Neh": 16, "Esth": 17, "Job": 18, "Ps": 19, "Prov": 20,
    "Eccl": 21, "Song": 22, "Isa": 23, "Jer": 24, "Lam": 25,
    "Ezek": 26, "Dan": 27, "Hos": 28, "Joel": 29, "Amos": 30,
    "Obad": 31, "Jonah": 32, "Mic": 33, "Nah": 34, "Hab": 35,
    "Zeph": 36, "Hag": 37, "Zech": 38, "Mal": 39,
    "Matt": 40, "Mark": 41, "Luke": 42, "John": 43, "Acts": 44,
    "Rom": 45, "1Cor": 46, "2Cor": 47, "Gal": 48, "Eph": 49,
    "Phil": 50, "Col": 51, "1Thess": 52, "2Thess": 53, "1Tim": 54,
    "2Tim": 55, "Titus": 56, "Phlm": 57, "Heb": 58, "Jas": 59,
    "1Pet": 60, "2Pet": 61, "1John": 62, "2John": 63, "3John": 64,
    "Jude": 65, "Rev": 66,
}

# OpenBible.info format: "Gen.1.1" or "1Sam.2.3"
OSIS_REF_PATTERN = re.compile(r"^(\d?\w+)\.(\d+)\.(\d+)")


def parse_osis_ref(ref: str) -> int | None:
    """Parse an OSIS reference like 'Gen.1.1' to a global verse ID (BBCCCVVV)."""
    m = OSIS_REF_PATTERN.match(ref.strip())
    if not m:
        return None
    book_abbr = m.group(1)
    book_num = OSIS_BOOK_MAP.get(book_abbr)
    if book_num is None:
        return None
    chapter = int(m.group(2))
    verse = int(m.group(3))
    return book_num * 1_000_000 + chapter * 1_000 + verse


def _import_openbible_crossrefs(file_path: Path, db: sqlite3.Connection) -> int:
    """
    Import OpenBible.info cross-references.

    Format: tab-separated with columns:
      From Verse \t To Verse \t Votes
    Example:
      Gen.1.1 \t John.1.1 \t 25
    """
    if not file_path.exists():
        print(f"    ⚠ {file_path.name} not found, skipping OpenBible cross-refs")
        return 0

    print(f"    → Importing cross-references from {file_path.name}")

    count = 0
    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or line.startswith("From"):
                continue

            parts = line.split("\t")
            if len(parts) < 2:
                continue

            source_id = parse_osis_ref(parts[0])
            target_id = parse_osis_ref(parts[1])

            if source_id is None or target_id is None:
                continue

            # Votes as confidence (normalize to 0-1 range, cap at 100)
            votes = 1.0
            if len(parts) >= 3:
                try:
                    raw_votes = int(parts[2])
                    votes = min(raw_votes / 100.0, 1.0)
                except ValueError:
                    votes = 0.5

            db.execute(
                """INSERT INTO cross_references (source_verse_id, target_verse_id, type, confidence)
                   VALUES (?, ?, ?, ?)""",
                (source_id, target_id, "related", votes),
            )
            count += 1

            if count % 10_000 == 0:
                db.commit()

    db.commit()
    return count


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import cross-reference data."""

    # OpenBible.info cross-references
    count_xref = _import_openbible_crossrefs(
        raw_dir / "cross-references" / "cross_references.txt",
        db,
    )
    if count_xref:
        print(f"      ✓ Cross-references: {count_xref:,} pairs")
