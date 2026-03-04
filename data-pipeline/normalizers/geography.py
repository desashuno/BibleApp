"""
geography.py — Normalizes geographic data into geographic_locations, location_verse_index,
and atlas_regions tables.

Sources:
  - openbibleinfo/Bible-Geocoding-Data (raw/geography/)
  - Theographic Bible Metadata Places.csv (raw/theographic/) — CC BY-SA 4.0

Output tables: geographic_locations, location_verse_index, atlas_regions
"""

from __future__ import annotations

import csv
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

# Theographic featureType → our type values
FEATURE_TYPE_MAP: dict[str, str] = {
    "city": "City",
    "mountain": "Mountain",
    "river": "River",
    "water": "Water",
    "region": "Region",
    "island": "Island",
    "landmark": "Landmark",
    "desert": "Desert",
    "valley": "Valley",
    "lake": "Water",
    "sea": "Water",
    "spring": "Water",
    "well": "Water",
    "town": "City",
    "village": "City",
    "wilderness": "Desert",
}

# Static biblical regions with approximate bounding boxes
BIBLICAL_REGIONS = [
    ("Canaan / Israel", "The Promised Land, central to biblical narrative",
     33.5, 29.5, 35.9, 34.0),
    ("Egypt", "Land of the pharaohs, site of the Exodus",
     31.5, 22.0, 35.0, 25.0),
    ("Mesopotamia", "Land between the rivers (Tigris and Euphrates)",
     37.5, 30.0, 48.0, 38.0),
    ("Asia Minor", "Modern Turkey, site of Paul's missionary journeys",
     42.0, 36.0, 44.0, 26.0),
    ("Greece", "Site of Paul's ministry in Corinth, Thessalonica, Athens",
     42.0, 35.0, 29.0, 19.5),
    ("Italy", "Rome, center of the Roman Empire",
     46.0, 36.5, 18.5, 6.5),
    ("Arabia", "Desert region east and south of Israel",
     32.0, 12.0, 55.0, 35.0),
    ("Persia", "Modern Iran, site of the Exile return under Cyrus",
     40.0, 25.0, 63.0, 44.0),
    ("Syria", "Region north of Israel, including Damascus and Antioch",
     37.5, 32.5, 42.0, 35.5),
    ("Phoenicia", "Coastal region of Tyre and Sidon",
     34.5, 33.0, 36.0, 35.0),
]


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
    for candidate in geo_dir.rglob("ancient.jsonl"):
        return candidate
    return None


def _derive_era(verse_refs: list[int]) -> str:
    """Derive era from verse references. OT books 1-39, NT books 40-66."""
    has_ot = False
    has_nt = False
    for gid in verse_refs:
        book = gid // 1_000_000
        if 1 <= book <= 39:
            has_ot = True
        elif 40 <= book <= 66:
            has_nt = True
        if has_ot and has_nt:
            return "AllEras"
    if has_ot:
        return "OldTestament"
    if has_nt:
        return "NewTestament"
    return "AllEras"


def _enrich_with_theographic(raw_dir: Path, db: sqlite3.Connection) -> int:
    """
    Enrich geographic_locations with Theographic Places.csv data.

    Matches by name (case-insensitive) and updates type/era fields.
    Returns number of locations enriched.
    """
    places_csv = raw_dir / "theographic" / "Places.csv"
    if not places_csv.exists():
        print("    ⚠ Theographic Places.csv not found, skipping enrichment")
        return 0

    print("    → Enriching locations with Theographic metadata")

    # Load Theographic places
    theographic: dict[str, dict] = {}  # lowercase name → row data
    with open(places_csv, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get("ESV_name", row.get("name", "")).strip()
            if name:
                theographic[name.lower()] = row

    # Fetch all locations from DB
    locations = db.execute(
        "SELECT id, name, lat, lon FROM geographic_locations"
    ).fetchall()

    enriched = 0
    for loc_id, loc_name, loc_lat, loc_lon in locations:
        match = theographic.get(loc_name.lower())

        # Try alternate matching if no direct match
        if not match:
            # Try without parenthetical suffixes
            base_name = re.sub(r"\s*\(.*\)", "", loc_name).strip()
            match = theographic.get(base_name.lower())

        if not match:
            # Try proximity-based matching (coordinates within 0.05°)
            for tname, trow in theographic.items():
                try:
                    tlat_str = trow.get("openBibleLat", "")
                    tlon_str = trow.get("openBibleLong", "")
                    if not tlat_str or not tlon_str:
                        continue
                    tlat = float(tlat_str)
                    tlon = float(tlon_str)
                    if abs(tlat - loc_lat) < 0.05 and abs(tlon - loc_lon) < 0.05:
                        match = trow
                        break
                except (ValueError, TypeError):
                    continue

        if not match:
            continue

        # Extract type from featureType
        feature_type = match.get("featureType", "").strip().lower()
        loc_type = FEATURE_TYPE_MAP.get(feature_type, "")

        if not loc_type:
            # Try featureSubType
            sub_type = match.get("featureSubType", "").strip().lower()
            loc_type = FEATURE_TYPE_MAP.get(sub_type, "")

        if loc_type:
            db.execute(
                "UPDATE geographic_locations SET type = ? WHERE id = ?",
                (loc_type, loc_id),
            )
            enriched += 1

    db.commit()
    return enriched


def _derive_eras_from_verses(db: sqlite3.Connection) -> int:
    """Derive era for each location based on its verse references."""
    print("    → Deriving eras from verse references")

    locations = db.execute(
        "SELECT id, verse_references FROM geographic_locations"
    ).fetchall()

    updated = 0
    for loc_id, verse_refs_json in locations:
        try:
            verse_refs = json.loads(verse_refs_json) if verse_refs_json else []
        except (json.JSONDecodeError, TypeError):
            continue

        if not verse_refs:
            continue

        era = _derive_era(verse_refs)
        if era != "AllEras":
            db.execute(
                "UPDATE geographic_locations SET era = ? WHERE id = ?",
                (era, loc_id),
            )
            updated += 1

    db.commit()
    return updated


def _insert_atlas_regions(db: sqlite3.Connection) -> int:
    """Insert static biblical region bounding boxes."""
    print("    → Inserting biblical regions")

    count = 0
    for name, desc, north, south, east, west in BIBLICAL_REGIONS:
        db.execute(
            """INSERT INTO atlas_regions
               (name, description, bounds_north, bounds_south, bounds_east, bounds_west)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (name, desc, north, south, east, west),
        )
        count += 1

    db.commit()
    return count


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Import geographic location data from OpenBible.info and enrich with Theographic."""
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

            # Collect verse references
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

            # Insert location (type and era will be enriched later)
            cur = db.execute(
                """INSERT INTO geographic_locations
                   (name, modern_name, lat, lon, type, description, era, verse_references)
                   VALUES (?, ?, ?, ?, '', ?, 'AllEras', ?)""",
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

    # Enrich with Theographic data (type)
    enriched = _enrich_with_theographic(raw_dir, db)
    if enriched:
        print(f"      ✓ Locations enriched with type: {enriched:,}")

    # Derive eras from verse references
    era_count = _derive_eras_from_verses(db)
    if era_count:
        print(f"      ✓ Locations with derived era: {era_count:,}")

    # Insert biblical regions
    region_count = _insert_atlas_regions(db)
    print(f"      ✓ Biblical regions: {region_count}")
