"""
commentaries.py -- Normalizes public-domain Bible commentary data into resources
and resource_entries tables.

Sources (all public domain):
  - Matthew Henry's Commentary        (raw/commentaries/matthew-henry/)
  - John Gill's Exposition            (raw/commentaries/john-gill/)
  - Jamieson-Fausset-Brown Commentary (raw/commentaries/jfb/)

Supported input formats: TSV, CSV, XML.

Output tables: resources, resource_entries
"""

from __future__ import annotations

import csv
import logging
import re
import sqlite3
import uuid
import xml.etree.ElementTree as ET
from pathlib import Path

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# UUID namespace for deterministic commentary UUIDs
# ---------------------------------------------------------------------------

_NAMESPACE = uuid.UUID("d7f8a3b1-9c2e-4f5a-b6d7-e8f9a0b1c2d3")


def _make_uuid(name: str) -> str:
    """Generate a deterministic UUID5 for a commentary resource."""
    return str(uuid.uuid5(_NAMESPACE, name))


# ---------------------------------------------------------------------------
# Book name / abbreviation -> canonical book number (1-66)
# ---------------------------------------------------------------------------

BOOK_MAP: dict[str, int] = {
    # Full names
    "genesis": 1, "exodus": 2, "leviticus": 3, "numbers": 4,
    "deuteronomy": 5, "joshua": 6, "judges": 7, "ruth": 8,
    "1 samuel": 9, "2 samuel": 10, "1 kings": 11, "2 kings": 12,
    "1 chronicles": 13, "2 chronicles": 14, "ezra": 15, "nehemiah": 16,
    "esther": 17, "job": 18, "psalms": 19, "psalm": 19,
    "proverbs": 20, "ecclesiastes": 21, "song of solomon": 22,
    "song of songs": 22, "isaiah": 23, "jeremiah": 24,
    "lamentations": 25, "ezekiel": 26, "daniel": 27, "hosea": 28,
    "joel": 29, "amos": 30, "obadiah": 31, "jonah": 32, "micah": 33,
    "nahum": 34, "habakkuk": 35, "zephaniah": 36, "haggai": 37,
    "zechariah": 38, "malachi": 39,
    "matthew": 40, "mark": 41, "luke": 42, "john": 43, "acts": 44,
    "romans": 45, "1 corinthians": 46, "2 corinthians": 47,
    "galatians": 48, "ephesians": 49, "philippians": 50,
    "colossians": 51, "1 thessalonians": 52, "2 thessalonians": 53,
    "1 timothy": 54, "2 timothy": 55, "titus": 56, "philemon": 57,
    "hebrews": 58, "james": 59, "1 peter": 60, "2 peter": 61,
    "1 john": 62, "2 john": 63, "3 john": 64, "jude": 65,
    "revelation": 66, "revelations": 66,

    # Common abbreviations
    "gen": 1, "ge": 1, "gn": 1,
    "exod": 2, "exo": 2, "ex": 2,
    "lev": 3, "le": 3, "lv": 3,
    "num": 4, "nu": 4, "nm": 4, "numb": 4,
    "deut": 5, "deu": 5, "de": 5, "dt": 5,
    "josh": 6, "jos": 6,
    "judg": 7, "jdg": 7, "jg": 7,
    "rut": 8, "ru": 8, "rth": 8,
    "1sam": 9, "1sa": 9, "1 sam": 9,
    "2sam": 10, "2sa": 10, "2 sam": 10,
    "1kgs": 11, "1ki": 11, "1 kgs": 11, "1 ki": 11,
    "2kgs": 12, "2ki": 12, "2 kgs": 12, "2 ki": 12,
    "1chr": 13, "1ch": 13, "1 chr": 13, "1 ch": 13,
    "2chr": 14, "2ch": 14, "2 chr": 14, "2 ch": 14,
    "ezr": 15,
    "neh": 16, "ne": 16,
    "esth": 17, "est": 17, "es": 17,
    "jb": 18,
    "ps": 19, "psa": 19, "pss": 19,
    "prov": 20, "pro": 20, "pr": 20, "prv": 20,
    "eccl": 21, "ecc": 21, "ec": 21, "qoh": 21,
    "song": 22, "sos": 22, "sg": 22, "canticles": 22, "cant": 22,
    "isa": 23, "is": 23,
    "jer": 24, "je": 24, "jr": 24,
    "lam": 25, "la": 25,
    "ezek": 26, "eze": 26, "ezk": 26,
    "dan": 27, "da": 27, "dn": 27,
    "hos": 28, "ho": 28,
    "joe": 29, "jl": 29,
    "amo": 30, "am": 30,
    "obad": 31, "oba": 31, "ob": 31,
    "jon": 32, "jnh": 32,
    "mic": 33, "mi": 33,
    "nah": 34, "na": 34,
    "hab": 35,
    "zeph": 36, "zep": 36,
    "hag": 37, "hg": 37,
    "zech": 38, "zec": 38,
    "mal": 39, "ml": 39,
    "matt": 40, "mat": 40, "mt": 40,
    "mrk": 41, "mk": 41, "mr": 41,
    "luk": 42, "lk": 42, "lu": 42,
    "jhn": 43, "jn": 43,
    "act": 44, "ac": 44,
    "rom": 45, "ro": 45, "rm": 45,
    "1cor": 46, "1co": 46, "1 cor": 46,
    "2cor": 47, "2co": 47, "2 cor": 47,
    "gal": 48, "ga": 48,
    "eph": 49,
    "phil": 50, "php": 50,
    "col": 51,
    "1thess": 52, "1th": 52, "1 thess": 52, "1 th": 52,
    "2thess": 53, "2th": 53, "2 thess": 53, "2 th": 53,
    "1tim": 54, "1ti": 54, "1 tim": 54, "1 ti": 54,
    "2tim": 55, "2ti": 55, "2 tim": 55, "2 ti": 55,
    "tit": 56,
    "phlm": 57, "phm": 57, "philem": 57,
    "heb": 58,
    "jas": 59, "jm": 59,
    "1pet": 60, "1pe": 60, "1pt": 60, "1 pet": 60, "1 pe": 60,
    "2pet": 61, "2pe": 61, "2pt": 61, "2 pet": 61, "2 pe": 61,
    "1jn": 62, "1jo": 62, "1 jn": 62, "1 john": 62,
    "2jn": 63, "2jo": 63, "2 jn": 63, "2 john": 63,
    "3jn": 64, "3jo": 64, "3 jn": 64, "3 john": 64,
    "jud": 65,
    "rev": 66, "re": 66, "rv": 66,
}


def _resolve_book(name: str) -> int | None:
    """Resolve a book name or abbreviation to its canonical number (1-66)."""
    if not name:
        return None
    key = name.strip().lower()
    # Direct lookup
    if key in BOOK_MAP:
        return BOOK_MAP[key]
    # Try stripping trailing period (e.g. "Gen.")
    if key.endswith("."):
        key2 = key[:-1]
        if key2 in BOOK_MAP:
            return BOOK_MAP[key2]
    return None


def _global_verse_id(book: int, chapter: int, verse: int) -> int:
    """Compute BBCCCVVV-format global verse ID."""
    return book * 1_000_000 + chapter * 1_000 + verse


# ---------------------------------------------------------------------------
# Reference parsing  (e.g. "Genesis 1:1", "Gen.1.1", "Gen 1:1", "01.001.001")
# ---------------------------------------------------------------------------

# Numeric-only pattern: BB.CCC.VVV or similar
_NUM_REF_RE = re.compile(r"^(\d{1,2})[.\s](\d{1,3})[.\s](\d{1,3})$")

# Book + chapter:verse  (handles "1 John 3:16", "Gen 1:1", "Gen.1.1")
_BOOK_REF_RE = re.compile(
    r"^(\d?\s*[A-Za-z][A-Za-z .]+?)\s*(\d{1,3})\s*[:.]?\s*(\d{1,3})$"
)


def _parse_reference(ref: str) -> tuple[int, int, int] | None:
    """
    Parse a scripture reference string into (book_num, chapter, verse).
    Returns None if the reference cannot be parsed.
    """
    ref = ref.strip()
    if not ref:
        return None

    # Try numeric format first  (e.g. "01.001.001")
    m = _NUM_REF_RE.match(ref)
    if m:
        book, chapter, verse = int(m.group(1)), int(m.group(2)), int(m.group(3))
        if 1 <= book <= 66 and 1 <= chapter <= 200 and 1 <= verse <= 200:
            return (book, chapter, verse)

    # Try book-name format
    m = _BOOK_REF_RE.match(ref)
    if m:
        book_name = m.group(1).strip()
        chapter = int(m.group(2))
        verse = int(m.group(3))
        book_num = _resolve_book(book_name)
        if book_num is not None:
            return (book_num, chapter, verse)

    return None


# ---------------------------------------------------------------------------
# Commentary metadata registry
# ---------------------------------------------------------------------------

COMMENTARY_DEFS: list[dict[str, str]] = [
    {
        "key": "matthew-henry",
        "title": "Matthew Henry's Commentary on the Whole Bible",
        "author": "Matthew Henry",
        "description": (
            "A comprehensive devotional commentary on every verse of the Bible, "
            "originally published between 1708 and 1710."
        ),
    },
    {
        "key": "john-gill",
        "title": "John Gill's Exposition of the Entire Bible",
        "author": "John Gill",
        "description": (
            "A detailed expository commentary covering every verse of the Bible, "
            "published between 1746 and 1766."
        ),
    },
    {
        "key": "jfb",
        "title": "Jamieson, Fausset, and Brown Commentary",
        "author": "Robert Jamieson, A. R. Fausset, David Brown",
        "description": (
            "A critical, explanatory, and practical commentary on the Old and "
            "New Testaments, first published in 1871."
        ),
    },
]


# ---------------------------------------------------------------------------
# Format-specific parsers
# ---------------------------------------------------------------------------

def _parse_tsv(file_path: Path) -> list[tuple[str, str]]:
    """
    Parse a TSV file with commentary data.

    Expected columns (flexible — detected by header or position):
      - A reference column (book/chapter/verse or combined reference)
      - A content/text column

    Common layouts:
      (A) reference<TAB>content
      (B) book<TAB>chapter<TAB>verse<TAB>content
    """
    entries: list[tuple[str, str]] = []
    try:
        with open(file_path, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.reader(f, delimiter="\t")
            header = None
            for row_num, row in enumerate(reader, start=1):
                if not row or all(c.strip() == "" for c in row):
                    continue
                # Skip comment lines
                if row[0].strip().startswith("#"):
                    continue
                # Detect header
                if header is None and row_num == 1:
                    lower_row = [c.lower().strip() for c in row]
                    if any(kw in lower_row for kw in ("book", "reference", "ref", "verse")):
                        header = lower_row
                        continue
                    # No header detected; treat first row as data
                    header = []

                ref_str, content = _extract_ref_content_from_row(row, header)
                if ref_str and content:
                    entries.append((ref_str, content))
    except Exception as e:
        logger.warning("Failed to parse TSV %s: %s", file_path.name, e)

    return entries


def _parse_csv(file_path: Path) -> list[tuple[str, str]]:
    """Parse a CSV file with commentary data (same logic as TSV but comma-delimited)."""
    entries: list[tuple[str, str]] = []
    try:
        with open(file_path, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.reader(f, delimiter=",")
            header = None
            for row_num, row in enumerate(reader, start=1):
                if not row or all(c.strip() == "" for c in row):
                    continue
                if row[0].strip().startswith("#"):
                    continue
                if header is None and row_num == 1:
                    lower_row = [c.lower().strip() for c in row]
                    if any(kw in lower_row for kw in ("book", "reference", "ref", "verse")):
                        header = lower_row
                        continue
                    header = []

                ref_str, content = _extract_ref_content_from_row(row, header)
                if ref_str and content:
                    entries.append((ref_str, content))
    except Exception as e:
        logger.warning("Failed to parse CSV %s: %s", file_path.name, e)

    return entries


def _extract_ref_content_from_row(
    row: list[str], header: list[str] | None
) -> tuple[str, str]:
    """
    Extract (reference_string, content) from a data row.

    Handles several column layouts:
      - 2 columns: reference, content
      - 3 columns: book, chapter_verse, content  OR  reference, unused, content
      - 4+ columns: book, chapter, verse, content
    Also tries to detect named columns via header.
    """
    if not row:
        return ("", "")

    # Named column detection
    if header:
        ref_idx = _find_col(header, ("reference", "ref", "verse_id", "verse_ref"))
        content_idx = _find_col(header, ("content", "text", "commentary", "comment", "body"))
        book_idx = _find_col(header, ("book", "book_name"))
        ch_idx = _find_col(header, ("chapter", "ch", "chap"))
        v_idx = _find_col(header, ("verse", "v", "verse_num"))

        if ref_idx is not None and content_idx is not None:
            ref_val = row[ref_idx].strip() if ref_idx < len(row) else ""
            cnt_val = row[content_idx].strip() if content_idx < len(row) else ""
            return (ref_val, cnt_val)

        if book_idx is not None and ch_idx is not None and v_idx is not None and content_idx is not None:
            book = row[book_idx].strip() if book_idx < len(row) else ""
            ch = row[ch_idx].strip() if ch_idx < len(row) else ""
            v = row[v_idx].strip() if v_idx < len(row) else ""
            cnt = row[content_idx].strip() if content_idx < len(row) else ""
            return (f"{book} {ch}:{v}", cnt)

    # Positional fallback
    ncols = len(row)
    if ncols >= 4:
        # Try: book, chapter, verse, content...
        book_str = row[0].strip()
        ch_str = row[1].strip()
        v_str = row[2].strip()
        content = " ".join(c.strip() for c in row[3:] if c.strip())
        if ch_str.isdigit() and v_str.isdigit():
            return (f"{book_str} {ch_str}:{v_str}", content)
        # Fall through to 2-col logic

    if ncols >= 2:
        # Assume first column is reference, last is content
        return (row[0].strip(), row[-1].strip())

    return ("", "")


def _find_col(header: list[str], names: tuple[str, ...]) -> int | None:
    """Find the index of the first matching column name in the header."""
    for i, col in enumerate(header):
        if col in names:
            return i
    return None


def _parse_xml(file_path: Path) -> list[tuple[str, str]]:
    """
    Parse an XML commentary file.

    Supports several common XML structures:
      (A) <bible>/<book name="Genesis">/<chapter num="1">/<verse num="1">content</verse>
      (B) <commentary>/<entry ref="Gen 1:1">content</entry>
      (C) <items>/<item book="Genesis" chapter="1" verse="1">content</item>
    """
    entries: list[tuple[str, str]] = []
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        # Strategy A: hierarchical <book>/<chapter>/<verse>
        for book_el in root.iter("book"):
            book_name = (
                book_el.get("name")
                or book_el.get("n")
                or book_el.get("title")
                or ""
            )
            for ch_el in book_el.iter("chapter"):
                ch_num = ch_el.get("num") or ch_el.get("n") or ch_el.get("number") or ""
                for v_el in ch_el.iter("verse"):
                    v_num = v_el.get("num") or v_el.get("n") or v_el.get("number") or ""
                    text = _xml_text(v_el)
                    if book_name and ch_num and v_num and text:
                        entries.append((f"{book_name} {ch_num}:{v_num}", text))

        # Strategy B: flat <entry ref="..."> elements
        if not entries:
            for entry_el in root.iter("entry"):
                ref = entry_el.get("ref") or entry_el.get("reference") or ""
                text = _xml_text(entry_el)
                if ref and text:
                    entries.append((ref, text))

        # Strategy C: flat <item> elements with separate attributes
        if not entries:
            for item_el in root.iter("item"):
                book = item_el.get("book") or ""
                ch = item_el.get("chapter") or ""
                v = item_el.get("verse") or ""
                text = _xml_text(item_el)
                if book and ch and v and text:
                    entries.append((f"{book} {ch}:{v}", text))

    except ET.ParseError as e:
        logger.warning("XML parse error in %s: %s", file_path.name, e)
    except Exception as e:
        logger.warning("Failed to parse XML %s: %s", file_path.name, e)

    return entries


def _xml_text(element: ET.Element) -> str:
    """Extract all text content from an XML element (including tail and children)."""
    parts = []
    if element.text:
        parts.append(element.text.strip())
    for child in element:
        child_text = _xml_text(child)
        if child_text:
            parts.append(child_text)
        if child.tail:
            parts.append(child.tail.strip())
    return " ".join(parts).strip()


# ---------------------------------------------------------------------------
# Unified file parser
# ---------------------------------------------------------------------------

_FORMAT_PARSERS = {
    ".tsv": _parse_tsv,
    ".csv": _parse_csv,
    ".xml": _parse_xml,
    ".txt": _parse_tsv,   # treat .txt as TSV (tab-separated)
}


def _parse_file(file_path: Path) -> list[tuple[str, str]]:
    """Dispatch to the appropriate parser based on file extension."""
    ext = file_path.suffix.lower()
    parser = _FORMAT_PARSERS.get(ext)
    if parser is None:
        logger.warning("Unsupported format %s for %s, skipping", ext, file_path.name)
        return []
    return parser(file_path)


# ---------------------------------------------------------------------------
# Import logic
# ---------------------------------------------------------------------------

def _already_imported(db: sqlite3.Connection, resource_uuid: str) -> bool:
    """Check whether a resource with this UUID already exists."""
    row = db.execute(
        "SELECT uuid FROM resources WHERE uuid = ?", (resource_uuid,)
    ).fetchone()
    return row is not None


def _import_commentary(
    db: sqlite3.Connection,
    commentary_dir: Path,
    meta: dict[str, str],
) -> int:
    """
    Import a single commentary from its directory.

    Looks for data files (*.tsv, *.csv, *.xml, *.txt) inside
    `commentary_dir` and processes each one.

    Returns the number of resource_entries inserted.
    """
    resource_uuid = _make_uuid(meta["key"])

    if _already_imported(db, resource_uuid):
        print(f"    - {meta['title']}: already imported, skipping")
        return 0

    # Collect all supported data files in the directory
    data_files: list[Path] = []
    if commentary_dir.is_dir():
        for ext in _FORMAT_PARSERS:
            data_files.extend(sorted(commentary_dir.glob(f"*{ext}")))
    elif commentary_dir.is_file() and commentary_dir.suffix.lower() in _FORMAT_PARSERS:
        # Single file rather than directory
        data_files = [commentary_dir]

    if not data_files:
        print(f"    - {meta['title']}: no data files found in {commentary_dir}")
        return 0

    # Parse all files and collect entries
    all_entries: list[tuple[int, str]] = []  # (global_verse_id, content)

    for fpath in data_files:
        raw_entries = _parse_file(fpath)
        for ref_str, content in raw_entries:
            parsed = _parse_reference(ref_str)
            if parsed is None:
                logger.debug("Unparseable reference '%s' in %s", ref_str, fpath.name)
                continue
            book, chapter, verse = parsed
            gvid = _global_verse_id(book, chapter, verse)
            all_entries.append((gvid, content))

    if not all_entries:
        print(f"    - {meta['title']}: no valid entries parsed from {len(data_files)} file(s)")
        return 0

    # Insert the resource row
    db.execute(
        """INSERT INTO resources (uuid, type, title, author, version, format)
           VALUES (?, 'commentary', ?, ?, '1.0', 'text')""",
        (resource_uuid, meta["title"], meta["author"]),
    )

    # Sort entries by verse ID for deterministic ordering, then insert
    all_entries.sort(key=lambda e: e[0])

    batch: list[tuple[str, int, str, int]] = []
    for sort_order, (gvid, content) in enumerate(all_entries):
        batch.append((resource_uuid, gvid, content, sort_order))
        if len(batch) >= 5_000:
            db.executemany(
                """INSERT INTO resource_entries (resource_id, global_verse_id, content, sort_order)
                   VALUES (?, ?, ?, ?)""",
                batch,
            )
            batch.clear()

    if batch:
        db.executemany(
            """INSERT INTO resource_entries (resource_id, global_verse_id, content, sort_order)
               VALUES (?, ?, ?, ?)""",
            batch,
        )

    db.commit()
    return len(all_entries)


# ---------------------------------------------------------------------------
# Public entry point
# ---------------------------------------------------------------------------

def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """
    Import all public-domain commentaries found under raw_dir/commentaries/.

    For each commentary defined in COMMENTARY_DEFS, looks for a matching
    subdirectory (or files) and imports verse-level entries into the
    resources / resource_entries tables.
    """
    commentaries_root = raw_dir / "commentaries"

    if not commentaries_root.exists():
        print("    - commentaries/ directory not found, skipping")
        return

    total = 0

    for meta in COMMENTARY_DEFS:
        key = meta["key"]
        # Look for a subdirectory named after the commentary key
        commentary_dir = commentaries_root / key

        # Also accept a single file named <key>.tsv / .csv / .xml / .txt
        if not commentary_dir.exists():
            for ext in _FORMAT_PARSERS:
                single_file = commentaries_root / f"{key}{ext}"
                if single_file.exists():
                    commentary_dir = single_file
                    break

        if not commentary_dir.exists():
            print(f"    - {meta['title']}: data not found ({key}/ or {key}.*)")
            continue

        print(f"    -> Importing: {meta['title']}")
        count = _import_commentary(db, commentary_dir, meta)
        if count:
            print(f"       {count:,} verse entries imported")
        total += count

    if total:
        print(f"    Total commentary entries: {total:,}")
    else:
        print("    No commentary data was imported (files may not be downloaded yet)")
