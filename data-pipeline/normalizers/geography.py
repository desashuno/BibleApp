"""
geography.py — Normalizes geographic data into geographic_locations and location_verse_index.

Sources:
  - openbibleinfo/Bible-Geocoding-Data (raw/geography/)

Output tables: geographic_locations, location_verse_index
"""

from __future__ import annotations

import json
import re
import sqlite3
from pathlib import Path

# OSIS book abbreviation → canonical number (1-66)
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


def _osis_to_global_id(ref: str) -> int | None:
    """Convert OSIS reference 'Gen.1.1' to BBCCCVVV."""
    m = re.match(r"^(\d?\w+)\.(\d+)\.(\d+)", ref)
    if not m:
        return None
    book_num = OSIS_BOOK_MAP.get(m.group(1))
    if book_num is None:
        return None
    return book_num * 1_000_000 + int(m.group(2)) * 1_000 + int(m.group(3))


def _find_ancient_jsonl(geo_dir: Path) -> Path | None:
    """Find the ancient.jsonl file in the downloaded geocoding archive."""
    # The archive extracts into a subfolder like Bible-Geocoding-Data-master/
    for candidate in geo_dir.rglob("ancient.jsonl"):
        return candidate
    return None


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import geographic location data from OpenBible.info geocoding data."""
    geo_dir = raw_dir / "geography"

    ancient_file = _find_ancient_jsonl(geo_dir)
    if not ancient_file:
        print("    ⚠ ancient.jsonl not found in geography/, skipping")
        return

    print(f"    → Importing locations from {ancient_file.name}")

    # Also load modern associations if available
    modern_file = ancient_file.parent / "modern.jsonl"
    modern_map: dict[str, str] = {}  # modern_id → modern name
    if modern_file.exists():
        with open(modern_file, "r", encoding="utf-8") as mf:
            for mline in mf:
                mline = mline.strip()
                if not mline:
                    continue
                try:
                    mobj = json.loads(mline)
                    mid = mobj.get("id", "")
                    mname = mobj.get("friendly_id", "") or mobj.get("name", "")
                    if mid and mname:
                        modern_map[mid] = mname
                except json.JSONDecodeError:
                    continue

    location_count = 0
    verse_link_count = 0

    with open(ancient_file, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            try:
                place = json.loads(line)
            except json.JSONDecodeError:
                continue

            # Extract place name from friendly_id
            name = place.get("friendly_id", "").strip()
            if not name:
                continue

            # Get coordinates from identifications → resolutions → lonlat
            lat = None
            lon = None
            modern_name = None
            description = ""

            identifications = place.get("identifications", [])
            for ident in identifications:
                # Description from identification
                desc = ident.get("description", "")
                if desc and not description:
                    # Strip HTML tags for clean text
                    description = re.sub(r"<[^>]+>", "", desc)

                # Modern name from identification id
                modern_id = ident.get("id", "")
                if modern_id and modern_id in modern_map and not modern_name:
                    modern_name = modern_map[modern_id]

                # Coordinates from resolutions
                resolutions = ident.get("resolutions", [])
                for res in resolutions:
                    lonlat_str = res.get("lonlat", "")
                    if lonlat_str and lat is None:
                        try:
                            parts = lonlat_str.split(",")
                            if len(parts) == 2:
                                lon = float(parts[0])
                                lat = float(parts[1])
                        except (ValueError, IndexError):
                            continue

                if lat is not None:
                    break  # Use first valid coordinates

            # Skip places without coordinates
            if lat is None or lon is None:
                continue

            # Collect verse references (verses is a list of dicts with "osis" key)
            verse_refs: list[int] = []
            verses_data = place.get("verses", [])
            for v_entry in verses_data:
                if isinstance(v_entry, dict):
                    ref = v_entry.get("osis", "")
                    if ref:
                        gid = _osis_to_global_id(ref)
                        if gid:
                            verse_refs.append(gid)
                elif isinstance(v_entry, str):
                    gid = _osis_to_global_id(v_entry)
                    if gid:
                        verse_refs.append(gid)

            verse_refs_json = json.dumps(verse_refs)

            # Insert location
            cur = db.execute(
                """INSERT INTO geographic_locations
                   (name, modern_name, lat, lon, description, verse_references)
                   VALUES (?, ?, ?, ?, ?, ?)""",
                (name, modern_name, lat, lon, description, verse_refs_json),
            )
            location_id = cur.lastrowid
            location_count += 1

            # Insert verse index entries
            for gid in verse_refs:
                db.execute(
                    "INSERT OR IGNORE INTO location_verse_index (location_id, global_verse_id) VALUES (?, ?)",
                    (location_id, gid),
                )
                verse_link_count += 1

            if location_count % 100 == 0:
                db.commit()

    db.commit()
    print(f"      ✓ Locations: {location_count:,}")
    print(f"      ✓ Location-verse links: {verse_link_count:,}")
