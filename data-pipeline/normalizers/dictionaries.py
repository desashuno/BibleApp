"""
dictionaries.py — Normalizes public-domain Bible dictionaries into dictionary_entries.

Sources:
  - Easton's Bible Dictionary (1893, public domain)
    raw/dictionaries/easton.json
  - Smith's Bible Dictionary (public domain)
    raw/dictionaries/smith.json

Output tables: resources, dictionary_entries, dictionary_entry_verses, fts_dictionary_entries
"""

from __future__ import annotations

import json
import logging
import re
import sqlite3
import uuid
from pathlib import Path

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Deterministic UUID namespace for dictionary resources
# ---------------------------------------------------------------------------

_DICT_UUID_NAMESPACE = uuid.UUID("d1c710a4-b1b1-4e3a-9f0e-abc123456789")


def _resource_uuid(title: str) -> str:
    """Generate a deterministic UUID5 for a dictionary resource."""
    return str(uuid.uuid5(_DICT_UUID_NAMESPACE, title))


# ---------------------------------------------------------------------------
# BOOK_MAP — canonical book names to book numbers (1-66)
# ---------------------------------------------------------------------------

BOOK_MAP: dict[str, int] = {}

_BOOK_NAMES = [
    # OT
    ("Genesis", 1), ("Exodus", 2), ("Leviticus", 3), ("Numbers", 4),
    ("Deuteronomy", 5), ("Joshua", 6), ("Judges", 7), ("Ruth", 8),
    ("1 Samuel", 9), ("2 Samuel", 10), ("1 Kings", 11), ("2 Kings", 12),
    ("1 Chronicles", 13), ("2 Chronicles", 14), ("Ezra", 15),
    ("Nehemiah", 16), ("Esther", 17), ("Job", 18), ("Psalms", 19),
    ("Proverbs", 20), ("Ecclesiastes", 21), ("Song of Solomon", 22),
    ("Isaiah", 23), ("Jeremiah", 24), ("Lamentations", 25),
    ("Ezekiel", 26), ("Daniel", 27), ("Hosea", 28), ("Joel", 29),
    ("Amos", 30), ("Obadiah", 31), ("Jonah", 32), ("Micah", 33),
    ("Nahum", 34), ("Habakkuk", 35), ("Zephaniah", 36), ("Haggai", 37),
    ("Zechariah", 38), ("Malachi", 39),
    # NT
    ("Matthew", 40), ("Mark", 41), ("Luke", 42), ("John", 43),
    ("Acts", 44), ("Romans", 45), ("1 Corinthians", 46),
    ("2 Corinthians", 47), ("Galatians", 48), ("Ephesians", 49),
    ("Philippians", 50), ("Colossians", 51), ("1 Thessalonians", 52),
    ("2 Thessalonians", 53), ("1 Timothy", 54), ("2 Timothy", 55),
    ("Titus", 56), ("Philemon", 57), ("Hebrews", 58), ("James", 59),
    ("1 Peter", 60), ("2 Peter", 61), ("1 John", 62), ("2 John", 63),
    ("3 John", 64), ("Jude", 65), ("Revelation", 66),
]

# Build the map with all common abbreviations and variants
for _name, _num in _BOOK_NAMES:
    BOOK_MAP[_name.lower()] = _num

# Common abbreviations
_ABBREVIATIONS: dict[str, int] = {
    "gen": 1, "ge": 1, "gn": 1,
    "exod": 2, "exo": 2, "ex": 2,
    "lev": 3, "le": 3, "lv": 3,
    "num": 4, "nu": 4, "nm": 4, "numb": 4,
    "deut": 5, "de": 5, "dt": 5, "deu": 5,
    "josh": 6, "jos": 6, "jsh": 6,
    "judg": 7, "jdg": 7, "jg": 7, "jdgs": 7,
    "rth": 8, "ru": 8, "rut": 8,
    "1 sam": 9, "1sam": 9, "1sa": 9, "1 sa": 9, "i sam": 9, "i samuel": 9,
    "2 sam": 10, "2sam": 10, "2sa": 10, "2 sa": 10, "ii sam": 10, "ii samuel": 10,
    "1 kgs": 11, "1kgs": 11, "1 ki": 11, "1ki": 11, "1 kin": 11, "1kin": 11,
    "i kings": 11, "i ki": 11, "1 king": 11,
    "2 kgs": 12, "2kgs": 12, "2 ki": 12, "2ki": 12, "2 kin": 12, "2kin": 12,
    "ii kings": 12, "ii ki": 12, "2 king": 12,
    "1 chr": 13, "1chr": 13, "1 ch": 13, "1ch": 13, "i chr": 13,
    "i chronicles": 13, "1 chron": 13, "1chron": 13,
    "2 chr": 14, "2chr": 14, "2 ch": 14, "2ch": 14, "ii chr": 14,
    "ii chronicles": 14, "2 chron": 14, "2chron": 14,
    "ezr": 15, "ez": 15,
    "neh": 16, "ne": 16,
    "est": 17, "esth": 17, "es": 17,
    "jb": 18,
    "ps": 19, "psa": 19, "pss": 19, "psalm": 19,
    "prov": 20, "pro": 20, "pr": 20, "prv": 20,
    "eccl": 21, "ecc": 21, "ec": 21, "eccles": 21, "qoh": 21,
    "song": 22, "sos": 22, "sng": 22, "canticles": 22, "cant": 22,
    "song of songs": 22, "songs": 22, "sol": 22, "so": 22,
    "isa": 23, "is": 23,
    "jer": 24, "je": 24, "jr": 24,
    "lam": 25, "la": 25,
    "ezek": 26, "eze": 26, "ezk": 26,
    "dan": 27, "da": 27, "dn": 27,
    "hos": 28, "ho": 28,
    "joe": 29, "jl": 29,
    "am": 30,
    "obad": 31, "ob": 31, "oba": 31,
    "jon": 32, "jnh": 32,
    "mic": 33, "mi": 33,
    "nah": 34, "na": 34,
    "hab": 35, "hb": 35,
    "zeph": 36, "zep": 36, "zp": 36,
    "hag": 37, "hg": 37,
    "zech": 38, "zec": 38, "zc": 38,
    "mal": 39, "ml": 39,
    "matt": 40, "mat": 40, "mt": 40,
    "mrk": 41, "mk": 41, "mr": 41, "mar": 41,
    "luk": 42, "lk": 42, "lu": 42,
    "jhn": 43, "jn": 43, "joh": 43,
    "act": 44, "ac": 44,
    "rom": 45, "ro": 45, "rm": 45,
    "1 cor": 46, "1cor": 46, "1 co": 46, "1co": 46, "i cor": 46,
    "i corinthians": 46, "1 corinth": 46,
    "2 cor": 47, "2cor": 47, "2 co": 47, "2co": 47, "ii cor": 47,
    "ii corinthians": 47, "2 corinth": 47,
    "gal": 48, "ga": 48,
    "eph": 49, "ep": 49, "ephes": 49,
    "phil": 50, "php": 50, "pp": 50,
    "col": 51, "co": 51,
    "1 thess": 52, "1thess": 52, "1 th": 52, "1th": 52, "i thess": 52,
    "i thessalonians": 52,
    "2 thess": 53, "2thess": 53, "2 th": 53, "2th": 53, "ii thess": 53,
    "ii thessalonians": 53,
    "1 tim": 54, "1tim": 54, "1 ti": 54, "1ti": 54, "i tim": 54,
    "i timothy": 54,
    "2 tim": 55, "2tim": 55, "2 ti": 55, "2ti": 55, "ii tim": 55,
    "ii timothy": 55,
    "tit": 56, "ti": 56,
    "phlm": 57, "phm": 57, "philem": 57,
    "heb": 58,
    "jas": 59, "jm": 59, "jam": 59,
    "1 pet": 60, "1pet": 60, "1 pe": 60, "1pe": 60, "i pet": 60,
    "i peter": 60,
    "2 pet": 61, "2pet": 61, "2 pe": 61, "2pe": 61, "ii pet": 61,
    "ii peter": 61,
    "1 jn": 62, "1jn": 62, "1 jo": 62, "1jo": 62, "i jn": 62,
    "i john": 62, "1 john": 62, "1john": 62,
    "2 jn": 63, "2jn": 63, "2 jo": 63, "2jo": 63, "ii jn": 63,
    "ii john": 63, "2 john": 63, "2john": 63,
    "3 jn": 64, "3jn": 64, "3 jo": 64, "3jo": 64, "iii jn": 64,
    "iii john": 64, "3 john": 64, "3john": 64,
    "jud": 65, "jde": 65,
    "rev": 66, "re": 66, "rv": 66, "apocalypse": 66,
}
BOOK_MAP.update(_ABBREVIATIONS)


# ---------------------------------------------------------------------------
# Verse reference parsing
# ---------------------------------------------------------------------------

# Pattern: "Book Chapter:Verse" with optional verse ranges
# Examples: "Genesis 1:1", "1 John 3:16", "Rev. 22:1-5"
_VERSE_REF_PATTERN = re.compile(
    r"""
    (?:(?:^|(?<=[\s;,(]))                       # start or preceded by separator
    ((?:[123I]+\s+)?                             # optional book number prefix
     [A-Z][a-z]+(?:\.\s|\s)?                     # book name (capitalized)
     (?:of\s)?                                   # optional "of" (Song of Solomon)
     (?:[A-Z][a-z]+)?                            # second word of book name
    )
    \s*
    (\d{1,3})                                    # chapter number
    :
    (\d{1,3})                                    # verse number
    (?:\s*-\s*(\d{1,3}))?                        # optional end verse
    )
    """,
    re.VERBOSE,
)


def _to_global_verse_id(book_num: int, chapter: int, verse: int) -> int:
    """Convert book/chapter/verse to BBCCCVVV format."""
    return book_num * 1_000_000 + chapter * 1_000 + verse


def _parse_book_name(raw: str) -> int | None:
    """Try to resolve a book name string to a book number."""
    cleaned = raw.strip().rstrip(".").strip().lower()
    if cleaned in BOOK_MAP:
        return BOOK_MAP[cleaned]
    # Try without trailing 's' (e.g., "Psalm" vs "Psalms")
    if cleaned.endswith("s") and cleaned[:-1] in BOOK_MAP:
        return BOOK_MAP[cleaned[:-1]]
    # Try adding 's'
    if cleaned + "s" in BOOK_MAP:
        return BOOK_MAP[cleaned + "s"]
    return None


def parse_verse_references(text: str) -> list[int]:
    """
    Extract global_verse_ids from text containing verse references.

    Returns a list of BBCCCVVV-format integers for each verse found.
    Handles ranges like "Genesis 1:1-3" by expanding to individual verses.
    """
    results: list[int] = []

    for match in _VERSE_REF_PATTERN.finditer(text):
        book_raw, chapter_str, verse_str, end_verse_str = match.groups()
        book_num = _parse_book_name(book_raw)
        if book_num is None:
            continue

        try:
            chapter = int(chapter_str)
            start_verse = int(verse_str)
        except ValueError:
            continue

        if end_verse_str:
            try:
                end_verse = int(end_verse_str)
            except ValueError:
                end_verse = start_verse
            # Cap range to avoid runaway
            end_verse = min(end_verse, start_verse + 50)
            for v in range(start_verse, end_verse + 1):
                gvid = _to_global_verse_id(book_num, chapter, v)
                results.append(gvid)
        else:
            gvid = _to_global_verse_id(book_num, chapter, start_verse)
            results.append(gvid)

    return results


# ---------------------------------------------------------------------------
# Dictionary metadata
# ---------------------------------------------------------------------------

DICTIONARIES = [
    {
        "key": "easton",
        "title": "Easton's Bible Dictionary",
        "author": "Matthew George Easton",
        "filename": "easton.json",
    },
    {
        "key": "smith",
        "title": "Smith's Bible Dictionary",
        "author": "William Smith",
        "filename": "smith.json",
    },
]


# ---------------------------------------------------------------------------
# Import logic
# ---------------------------------------------------------------------------

def _resource_exists(db: sqlite3.Connection, res_uuid: str) -> bool:
    """Check if a resource has already been imported."""
    row = db.execute(
        "SELECT 1 FROM resources WHERE uuid = ?", (res_uuid,)
    ).fetchone()
    return row is not None


def _insert_resource(
    db: sqlite3.Connection,
    res_uuid: str,
    title: str,
    author: str,
) -> None:
    """Insert a resource row for the dictionary."""
    db.execute(
        """INSERT INTO resources (uuid, type, title, author, version, format)
           VALUES (?, 'dictionary', ?, ?, '1.0', 'json')""",
        (res_uuid, title, author),
    )
    db.commit()


def _import_dictionary_json(
    db: sqlite3.Connection,
    file_path: Path,
    res_uuid: str,
    label: str,
) -> int:
    """
    Import a dictionary from a JSON file.

    Expected JSON structure (array of objects):
      [
        { "topic": "Aaron", "definition": "..." },
        { "topic": "Abel", "definition": "..." },
        ...
      ]

    Also handles alternative key names:
      - "headword" / "word" / "name" / "topic" / "title" for the headword
      - "definition" / "content" / "description" / "text" / "body" for the content
    """
    if not file_path.exists():
        print(f"    ⚠ {file_path.name} not found, skipping {label}")
        return 0

    print(f"    → Importing {label} from {file_path.name}")

    with open(file_path, "r", encoding="utf-8") as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError as e:
            print(f"    ⚠ Failed to parse {file_path.name}: {e}")
            return 0

    if not isinstance(data, list):
        print(f"    ⚠ Expected JSON array in {file_path.name}, got {type(data).__name__}")
        return 0

    entry_count = 0
    verse_link_count = 0

    for idx, entry in enumerate(data):
        if not isinstance(entry, dict):
            logger.warning("Skipping non-dict entry at index %d in %s", idx, file_path.name)
            continue

        # Extract headword from various possible keys
        headword = (
            entry.get("topic")
            or entry.get("headword")
            or entry.get("word")
            or entry.get("name")
            or entry.get("title")
            or ""
        )
        headword = str(headword).strip()

        # Extract content/definition from various possible keys
        content = (
            entry.get("definition")
            or entry.get("content")
            or entry.get("description")
            or entry.get("text")
            or entry.get("body")
            or ""
        )
        content = str(content).strip()

        if not headword or not content:
            logger.warning(
                "Skipping entry at index %d in %s: missing headword or content",
                idx, file_path.name,
            )
            continue

        # Extract related Strong's numbers if present
        related_strongs = entry.get("strongs", "") or entry.get("related_strongs", "")
        if isinstance(related_strongs, list):
            related_strongs = ",".join(str(s) for s in related_strongs)
        related_strongs = str(related_strongs).strip()

        # Insert dictionary entry
        cursor = db.execute(
            """INSERT INTO dictionary_entries
               (resource_id, headword, content, related_strongs, sort_order)
               VALUES (?, ?, ?, ?, ?)""",
            (res_uuid, headword, content, related_strongs, idx),
        )
        entry_id = cursor.lastrowid
        entry_count += 1

        # Parse verse references from the content
        verse_ids = parse_verse_references(content)
        if verse_ids:
            seen: set[int] = set()
            for gvid in verse_ids:
                if gvid not in seen:
                    seen.add(gvid)
                    db.execute(
                        """INSERT INTO dictionary_entry_verses (entry_id, global_verse_id)
                           VALUES (?, ?)""",
                        (entry_id, gvid),
                    )
                    verse_link_count += 1

        # Periodic commit for large dictionaries
        if entry_count % 500 == 0:
            db.commit()

    db.commit()

    if verse_link_count:
        print(f"      → {verse_link_count:,} verse links extracted")

    return entry_count


def _import_dictionary_tsv(
    db: sqlite3.Connection,
    file_path: Path,
    res_uuid: str,
    label: str,
) -> int:
    """
    Import a dictionary from a TSV file.

    Expected TSV format (tab-separated, first line may be a header):
      headword<TAB>definition
    """
    if not file_path.exists():
        print(f"    ⚠ {file_path.name} not found, skipping {label}")
        return 0

    print(f"    → Importing {label} from {file_path.name} (TSV)")

    entry_count = 0
    verse_link_count = 0

    with open(file_path, "r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, start=1):
            line = line.strip()
            if not line or line.startswith("#"):
                continue

            parts = line.split("\t")
            if len(parts) < 2:
                continue

            headword = parts[0].strip()
            content = parts[1].strip()

            # Skip header-like rows
            if line_num == 1 and headword.lower() in ("headword", "topic", "word", "term"):
                continue

            if not headword or not content:
                continue

            related_strongs = parts[2].strip() if len(parts) > 2 else ""

            cursor = db.execute(
                """INSERT INTO dictionary_entries
                   (resource_id, headword, content, related_strongs, sort_order)
                   VALUES (?, ?, ?, ?, ?)""",
                (res_uuid, headword, content, related_strongs, entry_count),
            )
            entry_id = cursor.lastrowid
            entry_count += 1

            # Parse verse references from the content
            verse_ids = parse_verse_references(content)
            if verse_ids:
                seen: set[int] = set()
                for gvid in verse_ids:
                    if gvid not in seen:
                        seen.add(gvid)
                        db.execute(
                            """INSERT INTO dictionary_entry_verses (entry_id, global_verse_id)
                               VALUES (?, ?)""",
                            (entry_id, gvid),
                        )
                        verse_link_count += 1

            if entry_count % 500 == 0:
                db.commit()

    db.commit()

    if verse_link_count:
        print(f"      → {verse_link_count:,} verse links extracted")

    return entry_count


def _import_dictionary(
    db: sqlite3.Connection,
    file_path: Path,
    res_uuid: str,
    label: str,
) -> int:
    """Import a dictionary, auto-detecting format by file extension."""
    suffix = file_path.suffix.lower()
    if suffix == ".json":
        return _import_dictionary_json(db, file_path, res_uuid, label)
    elif suffix in (".tsv", ".txt", ".csv"):
        return _import_dictionary_tsv(db, file_path, res_uuid, label)
    else:
        print(f"    ⚠ Unsupported file format '{suffix}' for {label}")
        return 0


# ---------------------------------------------------------------------------
# Public entry point
# ---------------------------------------------------------------------------

def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """
    Import public-domain Bible dictionaries.

    Looks for dictionary files in raw_dir / "dictionaries/".
    For each configured dictionary, inserts a resource row and
    populates dictionary_entries, dictionary_entry_verses, and
    fts_dictionary_entries.
    """
    dict_dir = raw_dir / "dictionaries"
    if not dict_dir.exists():
        print("    ⚠ No dictionaries/ directory found, skipping")
        return

    # Ensure the dictionary tables exist (in case schema was loaded
    # before migration 28 was applied)
    db.executescript("""
        CREATE TABLE IF NOT EXISTS dictionary_entries (
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            resource_id TEXT NOT NULL REFERENCES resources(uuid) ON DELETE CASCADE,
            headword TEXT NOT NULL,
            content TEXT NOT NULL,
            related_strongs TEXT,
            sort_order INTEGER NOT NULL DEFAULT 0
        );
        CREATE INDEX IF NOT EXISTS idx_dict_entries_resource
            ON dictionary_entries(resource_id);
        CREATE INDEX IF NOT EXISTS idx_dict_entries_headword
            ON dictionary_entries(resource_id, headword);

        CREATE TABLE IF NOT EXISTS dictionary_entry_verses (
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            entry_id INTEGER NOT NULL REFERENCES dictionary_entries(id) ON DELETE CASCADE,
            global_verse_id INTEGER NOT NULL
        );
        CREATE INDEX IF NOT EXISTS idx_dict_entry_verses_entry
            ON dictionary_entry_verses(entry_id);
        CREATE INDEX IF NOT EXISTS idx_dict_entry_verses_verse
            ON dictionary_entry_verses(global_verse_id);

        CREATE VIRTUAL TABLE IF NOT EXISTS fts_dictionary_entries USING fts5(
            headword, content,
            content=dictionary_entries, content_rowid=id
        );
    """)
    db.commit()

    total_entries = 0

    for dict_info in DICTIONARIES:
        title = dict_info["title"]
        author = dict_info["author"]
        filename = dict_info["filename"]
        res_uuid = _resource_uuid(title)

        # Skip if already imported
        if _resource_exists(db, res_uuid):
            print(f"    ✓ Already imported: {title}")
            continue

        file_path = dict_dir / filename
        if not file_path.exists():
            # Try alternative extensions
            for alt_ext in [".tsv", ".txt", ".xml"]:
                alt_path = dict_dir / (dict_info["key"] + alt_ext)
                if alt_path.exists():
                    file_path = alt_path
                    break

        if not file_path.exists():
            print(f"    ⚠ No file found for {title} (looked for {filename}), skipping")
            continue

        # Insert the resource row
        _insert_resource(db, res_uuid, title, author)

        # Import the dictionary entries
        count = _import_dictionary(db, file_path, res_uuid, title)
        if count:
            print(f"      ✓ {title}: {count:,} entries")
            total_entries += count
        else:
            print(f"      ⚠ {title}: no entries imported")

    # Rebuild FTS index for dictionary entries
    if total_entries > 0:
        try:
            db.execute(
                "INSERT INTO fts_dictionary_entries(fts_dictionary_entries) VALUES('rebuild')"
            )
            db.commit()
            fts_count = db.execute(
                "SELECT COUNT(*) FROM fts_dictionary_entries"
            ).fetchone()[0]
            print(f"      ✓ FTS index: {fts_count:,} entries indexed")
        except Exception as e:
            print(f"      ⚠ FTS rebuild failed: {e}")

    if total_entries == 0:
        print("    ⚠ No dictionary entries imported")
    else:
        print(f"    ✓ Total dictionary entries: {total_entries:,}")
