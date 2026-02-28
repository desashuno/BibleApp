"""
entities.py — Normalizes people/places/things data into entities, relationships, entity_verse_index.

Sources:
  - STEPBible TIPNR (raw/entities/TIPNR.txt) — Proper nouns with references

Output tables: entities, relationships, entity_verse_index
"""

from __future__ import annotations

import json
import re
import sqlite3
from pathlib import Path

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

VERSE_REF_PATTERN = re.compile(r"(\w+)\.(\d+)\.(\d+)")


def _parse_verse_refs(refs_str: str) -> list[int]:
    """Parse a string of verse references like 'Gen.1.1; Gen.1.2' into global IDs."""
    result = []
    for m in VERSE_REF_PATTERN.finditer(refs_str):
        book_abbr = m.group(1)
        book_num = STEP_BOOK_MAP.get(book_abbr)
        if book_num is None:
            continue
        chapter = int(m.group(2))
        verse = int(m.group(3))
        gid = book_num * 1_000_000 + chapter * 1_000 + verse
        result.append(gid)
    return result


def _classify_entity_type(type_str: str) -> str:
    """Classify TIPNR entity type into our EntityType values."""
    t = type_str.lower().strip()
    if "person" in t or "people" in t:
        return "Person"
    elif "place" in t or "location" in t or "city" in t or "region" in t:
        return "Place"
    elif "event" in t:
        return "Event"
    elif "object" in t or "thing" in t:
        return "Object"
    else:
        return "Concept"


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import entity data from STEPBible TIPNR."""
    tipnr_path = raw_dir / "entities" / "TIPNR.txt"

    if not tipnr_path.exists():
        print("    ⚠ TIPNR.txt not found, skipping entities")
        return

    print(f"    → Importing entities from TIPNR.txt")

    entity_count = 0
    relationship_count = 0
    verse_link_count = 0

    # Track entity IDs for relationship building
    name_to_entity_id: dict[str, int] = {}

    # TIPNR format: tab-separated, lines with data about individuals
    # Exact format varies by section but generally:
    #   Name \t UniqueName \t Type \t Parents \t Partners \t Siblings \t Offspring \t Refs \t Description
    with open(tipnr_path, "r", encoding="utf-8") as f:
        current_section = ""

        for line in f:
            line = line.strip()

            # Skip empty lines and comments
            if not line or line.startswith("#"):
                continue

            # Track section headers (prefixed with $)
            if line.startswith("$"):
                current_section = line
                continue

            parts = line.split("\t")
            if len(parts) < 3:
                continue

            # Extract entity data
            name = parts[0].strip()
            if not name or name.startswith("="):
                continue

            # Determine type from context
            entity_type = "Person"  # Default
            if len(parts) > 2:
                type_hint = parts[2].strip() if parts[2].strip() else ""
                if type_hint:
                    entity_type = _classify_entity_type(type_hint)

            # Check section for type hints
            section_lower = current_section.lower()
            if "place" in section_lower:
                entity_type = "Place"
            elif "person" in section_lower or "people" in section_lower:
                entity_type = "Person"

            # Unique name (for disambiguation)
            unique_name = parts[1].strip() if len(parts) > 1 else name

            # Collect aliases
            aliases = []
            if unique_name and unique_name != name:
                aliases.append(unique_name)

            aliases_json = json.dumps(aliases)

            # Parse verse references (look in later columns for refs)
            verse_refs: list[int] = []
            description = ""

            for i in range(3, len(parts)):
                col = parts[i].strip()
                # Check if this column contains verse references
                if VERSE_REF_PATTERN.search(col):
                    verse_refs.extend(_parse_verse_refs(col))
                # Check if this looks like a description (long text without refs)
                elif len(col) > 20 and not col.startswith("@"):
                    if not description:
                        description = col

            verse_refs = list(set(verse_refs))  # Deduplicate
            verse_refs_json = json.dumps(sorted(verse_refs))

            # Insert entity
            cur = db.execute(
                """INSERT INTO entities (name, type, description, aliases, verse_references)
                   VALUES (?, ?, ?, ?, ?)""",
                (name, entity_type, description or None, aliases_json, verse_refs_json),
            )
            entity_id = cur.lastrowid
            entity_count += 1

            name_to_entity_id[unique_name or name] = entity_id

            # Insert verse index entries
            for gid in verse_refs:
                db.execute(
                    "INSERT OR IGNORE INTO entity_verse_index (entity_id, global_verse_id) VALUES (?, ?)",
                    (entity_id, gid),
                )
                verse_link_count += 1

            if entity_count % 500 == 0:
                db.commit()

    db.commit()

    # Second pass: build relationships from parent/partner/sibling/offspring columns
    # This requires re-reading the file with knowledge of entity IDs
    print("    → Building entity relationships")

    with open(tipnr_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or line.startswith("$"):
                continue

            parts = line.split("\t")
            if len(parts) < 4:
                continue

            name = parts[0].strip()
            unique_name = parts[1].strip() if len(parts) > 1 else name
            source_key = unique_name or name
            source_id = name_to_entity_id.get(source_key)
            if source_id is None:
                continue

            # Relationship columns (varies by format, look for known entity names)
            relationship_types = [
                (3, "ParentOf"),    # Parents column
                (4, "SpouseOf"),    # Partners column
                (5, "SiblingOf"),   # Siblings column
                (6, "ParentOf"),    # Offspring column (reversed)
            ]

            for col_idx, rel_type in relationship_types:
                if col_idx >= len(parts):
                    continue

                col = parts[col_idx].strip()
                if not col or col == "–" or col == "-":
                    continue

                # Try to match referenced entity names
                referenced_names = [n.strip() for n in col.split(";") if n.strip()]
                for ref_name in referenced_names:
                    target_id = name_to_entity_id.get(ref_name)
                    if target_id and target_id != source_id:
                        # For offspring, reverse the relationship
                        if col_idx == 6:
                            db.execute(
                                """INSERT INTO relationships
                                   (source_entity_id, target_entity_id, type, description)
                                   VALUES (?, ?, ?, NULL)""",
                                (target_id, source_id, rel_type),
                            )
                        else:
                            db.execute(
                                """INSERT INTO relationships
                                   (source_entity_id, target_entity_id, type, description)
                                   VALUES (?, ?, ?, NULL)""",
                                (source_id, target_id, rel_type),
                            )
                        relationship_count += 1

    db.commit()

    print(f"      ✓ Entities: {entity_count:,}")
    print(f"      ✓ Relationships: {relationship_count:,}")
    print(f"      ✓ Entity-verse links: {verse_link_count:,}")
