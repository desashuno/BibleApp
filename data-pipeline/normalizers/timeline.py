"""
timeline.py — Normalizes timeline event data into timeline_events and timeline_event_verses.

Sources:
  - Seed data generated from well-known biblical chronology (public domain knowledge)
  - Theographic Bible Metadata Events.csv (raw/theographic/) — CC BY-SA 4.0

Output tables: timeline_events, timeline_event_verses
"""

from __future__ import annotations

import csv
import re
import sqlite3
from pathlib import Path

# Theographic/OSIS book abbreviation → canonical book number
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
    # Theographic also uses short forms
    "Ge": 1, "Ex": 2, "Le": 3, "Nu": 4, "De": 5,
    "Jos": 6, "Jdg": 7, "Rut": 8, "1Sa": 9, "2Sa": 10,
    "1Ki": 11, "2Ki": 12, "1Ch": 13, "2Ch": 14, "Ezr": 15,
    "Ne": 16, "Es": 17, "Ps": 19, "Pr": 20,
    "Ec": 21, "So": 22, "Isa": 23, "Jer": 24, "La": 25,
    "Eze": 26, "Da": 27, "Ho": 28, "Joe": 29, "Am": 30,
    "Ob": 31, "Jon": 32, "Mic": 33, "Na": 34, "Hab": 35,
    "Zep": 36, "Hag": 37, "Zec": 38, "Mal": 39,
    "Mt": 40, "Mk": 41, "Lk": 42, "Jn": 43, "Ac": 44,
    "Ro": 45, "1Co": 46, "2Co": 47, "Ga": 48, "Eph": 49,
    "Php": 50, "Col": 51, "1Th": 52, "2Th": 53, "1Ti": 54,
    "2Ti": 55, "Tit": 56, "Phm": 57, "Heb": 58, "Jas": 59,
    "1Pe": 60, "2Pe": 61, "1Jn": 62, "2Jn": 63, "3Jn": 64,
    "Jud": 65, "Rev": 66,
    # Theographic specific
    "Genesis": 1, "Exodus": 2, "Leviticus": 3, "Numbers": 4, "Deuteronomy": 5,
    "Joshua": 6, "Judges": 7, "1 Samuel": 9, "2 Samuel": 10,
    "1 Kings": 11, "2 Kings": 12, "1 Chronicles": 13, "2 Chronicles": 14,
    "Nehemiah": 16, "Esther": 17, "Psalms": 19, "Proverbs": 20,
    "Ecclesiastes": 21, "Isaiah": 23, "Jeremiah": 24, "Lamentations": 25,
    "Ezekiel": 26, "Daniel": 27, "Hosea": 28, "Joel": 29, "Amos": 30,
    "Obadiah": 31, "Jonah": 32, "Micah": 33, "Nahum": 34, "Habakkuk": 35,
    "Zephaniah": 36, "Haggai": 37, "Zechariah": 38, "Malachi": 39,
    "Matthew": 40, "Mark": 41, "Luke": 42, "John": 43, "Acts": 44,
    "Romans": 45, "1 Corinthians": 46, "2 Corinthians": 47, "Galatians": 48,
    "Ephesians": 49, "Philippians": 50, "Colossians": 51,
    "1 Thessalonians": 52, "2 Thessalonians": 53,
    "1 Timothy": 54, "2 Timothy": 55, "Titus": 56, "Philemon": 57,
    "Hebrews": 58, "James": 59, "1 Peter": 60, "2 Peter": 61,
    "1 John": 62, "2 John": 63, "3 John": 64, "Jude": 65, "Revelation": 66,
}

# Seed timeline events: (title, description, year_start, year_end, era, importance, verse_ids)
SEED_EVENTS: list[tuple[str, str, int, int | None, str, int, list[int]]] = [
    # Creation & Patriarchs
    ("Creation", "God creates the heavens and the earth",
     -4004, None, "Creation", 3, [1001001]),
    ("The Fall", "Adam and Eve disobey God in the Garden of Eden",
     -4004, None, "Creation", 3, [1003006]),
    ("The Flood", "God sends a worldwide flood; Noah builds the ark",
     -2348, None, "Creation", 3, [1006017]),
    ("Tower of Babel", "Humanity attempts to build a tower to heaven",
     -2242, None, "Creation", 2, [1011001]),
    ("Call of Abraham", "God calls Abram to leave Ur and go to Canaan",
     -2091, None, "Patriarchs", 3, [1012001]),
    ("Covenant with Abraham", "God establishes His covenant with Abraham",
     -2067, None, "Patriarchs", 2, [1015001]),
    ("Binding of Isaac", "Abraham is tested by God with the sacrifice of Isaac",
     -2054, None, "Patriarchs", 2, [1022001]),
    ("Jacob and Esau", "Birth of Jacob and Esau to Isaac and Rebekah",
     -2006, None, "Patriarchs", 1, [1025019]),
    ("Joseph sold into slavery", "Joseph's brothers sell him to traders going to Egypt",
     -1898, None, "Patriarchs", 2, [1037028]),
    ("Jacob's family moves to Egypt", "Israel settles in Egypt during the famine",
     -1876, None, "Patriarchs", 2, [1046001]),

    # Exodus & Conquest
    ("Birth of Moses", "Moses is born in Egypt and placed in the Nile",
     -1527, None, "Exodus", 2, [2002001]),
    ("The Burning Bush", "God appears to Moses in a burning bush",
     -1447, None, "Exodus", 2, [2003001]),
    ("The Ten Plagues", "God sends ten plagues upon Egypt",
     -1446, None, "Exodus", 2, [2007014]),
    ("The Exodus", "Israel leaves Egypt, crossing the Red Sea",
     -1446, None, "Exodus", 3, [2014001]),
    ("Giving of the Law at Sinai", "God gives the Ten Commandments to Moses",
     -1446, None, "Exodus", 3, [2020001]),
    ("40 Years in the Wilderness", "Israel wanders in the desert for 40 years",
     -1446, -1406, "Exodus", 2, [4014033]),
    ("Death of Moses", "Moses views the Promised Land and dies",
     -1406, None, "Exodus", 2, [5034001]),
    ("Conquest of Canaan", "Joshua leads Israel into the Promised Land",
     -1406, -1375, "Exodus", 3, [6001001]),
    ("Fall of Jericho", "The walls of Jericho fall before Israel",
     -1406, None, "Exodus", 2, [6006020]),

    # Judges
    ("Period of the Judges", "Cycles of apostasy, oppression, and deliverance",
     -1375, -1050, "Judges", 2, [7002016]),
    ("Deborah judges Israel", "Deborah leads Israel to victory over Sisera",
     -1209, None, "Judges", 1, [7004004]),
    ("Gideon defeats the Midianites", "Gideon's 300 men defeat the Midianite army",
     -1169, None, "Judges", 1, [7007001]),
    ("Samson", "Samson judges Israel for 20 years",
     -1075, -1055, "Judges", 1, [7013024]),
    ("Ruth and Boaz", "Ruth's loyalty and marriage to Boaz",
     -1100, None, "Judges", 1, [8001001]),
    ("Birth of Samuel", "Hannah dedicates Samuel to the Lord",
     -1105, None, "Judges", 1, [9001020]),

    # United Monarchy
    ("Saul anointed king", "Samuel anoints Saul as first king of Israel",
     -1050, None, "United Monarchy", 2, [9010001]),
    ("David anointed by Samuel", "God chooses David to replace Saul",
     -1025, None, "United Monarchy", 2, [9016013]),
    ("David defeats Goliath", "Young David kills the Philistine giant",
     -1024, None, "United Monarchy", 2, [9017049]),
    ("David becomes king", "David is crowned king over all Israel",
     -1010, None, "United Monarchy", 3, [10005003]),
    ("Solomon builds the Temple", "Construction of the First Temple in Jerusalem",
     -966, -959, "United Monarchy", 3, [11006001]),
    ("Dedication of the Temple", "Solomon dedicates the Temple to the Lord",
     -959, None, "United Monarchy", 2, [11008001]),
    ("Death of Solomon", "Solomon dies; the kingdom is divided",
     -931, None, "United Monarchy", 2, [11011043]),

    # Divided Monarchy
    ("Kingdom divided", "Rehoboam and Jeroboam split the kingdom",
     -931, None, "Divided Monarchy", 3, [11012001]),
    ("Elijah on Mount Carmel", "Elijah challenges the prophets of Baal",
     -869, None, "Divided Monarchy", 2, [11018001]),
    ("Fall of Samaria (Northern Kingdom)", "Assyria conquers the Northern Kingdom of Israel",
     -722, None, "Divided Monarchy", 3, [12017006]),
    ("Hezekiah's reforms", "King Hezekiah restores worship in Judah",
     -715, None, "Divided Monarchy", 1, [12018001]),
    ("Josiah's reforms", "King Josiah rediscovers the Law and reforms Judah",
     -622, None, "Divided Monarchy", 2, [12022001]),

    # Exile
    ("Fall of Jerusalem", "Babylon destroys Jerusalem and the Temple",
     -586, None, "Exile", 3, [12025001]),
    ("Babylonian Exile begins", "Judah is taken into captivity in Babylon",
     -586, -538, "Exile", 3, [24052001]),
    ("Daniel in Babylon", "Daniel serves in the Babylonian and Persian courts",
     -605, -536, "Exile", 2, [27001001]),
    ("Fiery furnace", "Shadrach, Meshach, and Abednego in the furnace",
     -586, None, "Exile", 1, [27003001]),
    ("Daniel in the lion's den", "Daniel is thrown into the den of lions",
     -539, None, "Exile", 2, [27006001]),

    # Return & Restoration
    ("Decree of Cyrus", "Cyrus the Great allows Jews to return to Jerusalem",
     -538, None, "Return", 3, [15001001]),
    ("Second Temple built", "Zerubbabel completes the rebuilding of the Temple",
     -516, None, "Return", 3, [15006015]),
    ("Ezra returns to Jerusalem", "Ezra leads a group back and restores the Law",
     -458, None, "Return", 2, [15007001]),
    ("Nehemiah rebuilds the walls", "Nehemiah leads the rebuilding of Jerusalem's walls",
     -445, None, "Return", 2, [16002017]),
    ("Esther saves her people", "Esther intervenes to prevent the destruction of the Jews",
     -473, None, "Return", 2, [17004001]),

    # Intertestamental
    ("Intertestamental period", "400 years between Malachi and the NT events",
     -430, -5, "Intertestamental", 2, []),

    # New Testament
    ("Birth of Jesus", "Jesus is born in Bethlehem",
     -5, None, "New Testament", 3, [42002007]),
    ("Baptism of Jesus", "John baptizes Jesus in the Jordan River",
     26, None, "New Testament", 3, [40003013]),
    ("Sermon on the Mount", "Jesus delivers the Sermon on the Mount",
     27, None, "New Testament", 2, [40005001]),
    ("Feeding of the 5000", "Jesus feeds 5000 with five loaves and two fish",
     29, None, "New Testament", 1, [43006001]),
    ("Transfiguration", "Jesus is transfigured on the mountain",
     29, None, "New Testament", 2, [40017001]),
    ("Triumphal Entry", "Jesus enters Jerusalem on a donkey",
     30, None, "New Testament", 2, [40021001]),
    ("The Last Supper", "Jesus shares the Passover meal with His disciples",
     30, None, "New Testament", 3, [40026017]),
    ("Crucifixion of Jesus", "Jesus is crucified at Golgotha",
     30, None, "New Testament", 3, [40027035]),
    ("Resurrection of Jesus", "Jesus rises from the dead on the third day",
     30, None, "New Testament", 3, [40028006]),
    ("Ascension of Jesus", "Jesus ascends to heaven from the Mount of Olives",
     30, None, "New Testament", 3, [44001009]),
    ("Day of Pentecost", "The Holy Spirit descends upon the disciples",
     30, None, "New Testament", 3, [44002001]),

    # Early Church
    ("Conversion of Paul", "Saul encounters Jesus on the road to Damascus",
     34, None, "Early Church", 3, [44009001]),
    ("Paul's first missionary journey", "Paul and Barnabas travel through Asia Minor",
     46, 48, "Early Church", 2, [44013001]),
    ("Council of Jerusalem", "The apostles decide on Gentile inclusion",
     49, None, "Early Church", 2, [44015001]),
    ("Paul's second missionary journey", "Paul travels through Greece",
     49, 52, "Early Church", 2, [44015040]),
    ("Paul's third missionary journey", "Paul revisits churches in Asia Minor and Greece",
     53, 57, "Early Church", 2, [44018023]),
    ("Paul arrested in Jerusalem", "Paul is arrested and appeals to Caesar",
     57, None, "Early Church", 1, [44021030]),
    ("Paul in Rome", "Paul arrives in Rome under house arrest",
     60, 62, "Early Church", 2, [44028016]),
    ("Destruction of the Temple", "Romans destroy the Second Temple in Jerusalem",
     70, None, "Early Church", 3, []),
    ("John writes Revelation", "The apostle John receives visions on Patmos",
     95, None, "Early Church", 2, [66001001]),
]

# Major events that get importance=3 in Theographic import
MAJOR_EVENT_KEYWORDS = {
    "creation", "flood", "exodus", "sinai", "covenant", "temple",
    "exile", "crucifixion", "resurrection", "pentecost", "birth of jesus",
    "fall of jerusalem", "conquest of canaan", "david", "solomon",
}


def _parse_osis_ref(ref: str) -> int | None:
    """Parse OSIS-style reference like 'Gen.1.1' to BBCCCVVV global verse ID."""
    m = re.match(r"^(\d?\s*\w+)\.(\d+)\.(\d+)", ref.strip())
    if not m:
        return None
    book_str = m.group(1).strip()
    book_num = OSIS_BOOK_MAP.get(book_str)
    if book_num is None:
        return None
    return book_num * 1_000_000 + int(m.group(2)) * 1_000 + int(m.group(3))


def _parse_theographic_refs(refs_str: str) -> list[int]:
    """Parse Theographic verse references (semicolon-separated OSIS refs)."""
    result = []
    if not refs_str:
        return result
    for ref in refs_str.split(";"):
        ref = ref.strip()
        if not ref:
            continue
        # Try direct OSIS parse
        gid = _parse_osis_ref(ref)
        if gid:
            result.append(gid)
            continue
        # Try chapter-only format "Gen 1" → take verse 1
        m = re.match(r"^(\d?\s*\w+)\s+(\d+)$", ref)
        if m:
            book_str = m.group(1).strip()
            book_num = OSIS_BOOK_MAP.get(book_str)
            if book_num:
                result.append(book_num * 1_000_000 + int(m.group(2)) * 1_000 + 1)
    return result


def _parse_theographic_date(date_str: str) -> int | None:
    """Parse Theographic startDate. Negative = BCE, positive = CE."""
    if not date_str:
        return None
    date_str = date_str.strip()
    try:
        return int(date_str)
    except ValueError:
        # Try extracting number
        m = re.match(r"(-?\d+)", date_str)
        if m:
            return int(m.group(1))
        return None


def _parse_duration(dur_str: str) -> int | None:
    """Parse Theographic duration like '7D', '930Y', '40Y' into years (approximate)."""
    if not dur_str:
        return None
    dur_str = dur_str.strip().upper()
    m = re.match(r"(\d+)([DWMmY]?)", dur_str)
    if not m:
        return None
    value = int(m.group(1))
    unit = m.group(2)
    if unit == "D":
        return max(1, value // 365)  # approximate to years
    if unit == "W":
        return max(1, value // 52)
    if unit in ("M", "m"):
        return max(1, value // 12)
    return value  # default: years


def _classify_importance(title: str) -> int:
    """Assign importance 1-3 based on event title."""
    title_lower = title.lower()
    for keyword in MAJOR_EVENT_KEYWORDS:
        if keyword in title_lower:
            return 3
    return 1


def _import_theographic_events(raw_dir: Path, db: sqlite3.Connection, existing_titles: set[str]) -> int:
    """Import events from Theographic Events.csv, deduplicating against existing."""
    events_csv = raw_dir / "theographic" / "Events.csv"
    if not events_csv.exists():
        print("    ⚠ Theographic Events.csv not found, skipping")
        return 0

    print("    → Importing Theographic events")

    count = 0
    with open(events_csv, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            title = row.get("title", row.get("name", "")).strip()
            if not title:
                continue

            # Skip duplicates (case-insensitive match against seed events)
            if title.lower() in existing_titles:
                continue

            year_start = _parse_theographic_date(row.get("startDate", ""))
            if year_start is None:
                continue

            description = row.get("description", "").strip() or None

            # Calculate year_end from duration
            year_end = None
            duration = _parse_duration(row.get("duration", ""))
            if duration and duration > 0:
                year_end = year_start + duration

            # Determine era from year
            if year_start < -1500:
                era = "Patriarchs"
            elif year_start < -1050:
                era = "Exodus"
            elif year_start < -931:
                era = "United Monarchy"
            elif year_start < -586:
                era = "Divided Monarchy"
            elif year_start < -538:
                era = "Exile"
            elif year_start < -5:
                era = "Return"
            elif year_start < 33:
                era = "New Testament"
            else:
                era = "Early Church"

            importance = _classify_importance(title)

            cur = db.execute(
                """INSERT INTO timeline_events
                   (title, description, year_start, year_end, era, importance)
                   VALUES (?, ?, ?, ?, ?, ?)""",
                (title, description, year_start, year_end, era, importance),
            )
            event_id = cur.lastrowid

            # Parse verse references
            verse_refs = _parse_theographic_refs(row.get("verses", ""))
            for gid in verse_refs:
                db.execute(
                    "INSERT INTO timeline_event_verses (event_id, global_verse_id) VALUES (?, ?)",
                    (event_id, gid),
                )

            existing_titles.add(title.lower())
            count += 1

    db.commit()
    return count


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Insert seed timeline events and import Theographic events."""
    print("    → Inserting timeline events")

    existing_titles: set[str] = set()
    count = 0
    for title, description, year_start, year_end, era, importance, verse_ids in SEED_EVENTS:
        cur = db.execute(
            """INSERT INTO timeline_events
               (title, description, year_start, year_end, era, importance)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (title, description, year_start, year_end, era, importance),
        )
        event_id = cur.lastrowid

        # Insert verse links into join table
        for gid in verse_ids:
            db.execute(
                "INSERT INTO timeline_event_verses (event_id, global_verse_id) VALUES (?, ?)",
                (event_id, gid),
            )

        existing_titles.add(title.lower())
        count += 1

    db.commit()
    print(f"      ✓ Seed timeline events: {count}")

    # Import Theographic events
    theo_count = _import_theographic_events(raw_dir, db, existing_titles)
    if theo_count:
        print(f"      ✓ Theographic events: {theo_count}")

    total = db.execute("SELECT COUNT(*) FROM timeline_events").fetchone()[0]
    verse_links = db.execute("SELECT COUNT(*) FROM timeline_event_verses").fetchone()[0]
    print(f"      ✓ Total timeline events: {total}")
    print(f"      ✓ Timeline-verse links: {verse_links}")
