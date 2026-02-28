"""
reading_plans.py — Generates reading plan data for the reading_plans table.

These are not downloaded from external sources but generated from
the canonical Bible structure (66 books, 1189 chapters).

Output table: reading_plans
"""

from __future__ import annotations

import sqlite3
from pathlib import Path

# Chapter counts per book (1-66)
CHAPTER_COUNTS = [
    50, 40, 27, 36, 34, 24, 21, 4, 31, 24,    # Gen-2Sam     (1-10)
    22, 25, 29, 36, 10, 13, 10, 42, 150, 31,  # 1Ki-Prov     (11-20)
    12, 8, 66, 52, 5, 48, 12, 14, 3, 9,       # Ecc-Amos     (21-30)
    1, 4, 7, 3, 3, 3, 2, 14, 4,               # Obad-Mal     (31-39)
    28, 16, 24, 21, 28, 16, 16, 13, 6, 6,     # Matt-Eph     (40-49)
    4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5,         # Phil-1Pet    (50-60)
    3, 5, 1, 1, 1, 22,                         # 2Pet-Rev     (61-66)
]

BOOK_NAMES = [
    "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
    "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
    "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
    "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
    "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
    "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
    "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
    "Zephaniah", "Haggai", "Zechariah", "Malachi",
    "Matthew", "Mark", "Luke", "John", "Acts",
    "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
    "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
    "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
    "1 Peter", "2 Peter", "1 John", "2 John", "3 John",
    "Jude", "Revelation",
]

# Total chapters: 1189
TOTAL_CHAPTERS = sum(CHAPTER_COUNTS)


def _all_chapters() -> list[tuple[int, int]]:
    """Return all (book_number, chapter) pairs in canonical order."""
    result = []
    for book_idx, ch_count in enumerate(CHAPTER_COUNTS):
        book_num = book_idx + 1
        for ch in range(1, ch_count + 1):
            result.append((book_num, ch))
    return result


def _chapters_for_books(book_range: range) -> list[tuple[int, int]]:
    """Return (book_number, chapter) pairs for a range of book indices (0-based)."""
    result = []
    for book_idx in book_range:
        book_num = book_idx + 1
        for ch in range(1, CHAPTER_COUNTS[book_idx] + 1):
            result.append((book_num, ch))
    return result


def _build_daily_assignments(chapters: list[tuple[int, int]], days: int) -> list[list[tuple[int, int]]]:
    """Distribute chapters across the given number of days."""
    daily: list[list[tuple[int, int]]] = [[] for _ in range(days)]
    for i, ch in enumerate(chapters):
        daily[i % days].append(ch)
    return daily


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Generate reading plans."""
    print("    → Generating reading plans")

    # Fixed UUIDs must match BuiltInPlans.kt in the app
    plans = [
        {
            "uuid": "built-in-bible-in-a-year",
            "title": "Bible in a Year",
            "description": "Read through the entire Bible in 365 days following a chronological order.",
            "duration_days": 365,
            "type": "chronological",
            "chapters": _all_chapters(),
        },
        {
            "uuid": "built-in-nt-90-days",
            "title": "New Testament in 90 Days",
            "description": "Complete the New Testament in 90 days with focused daily readings.",
            "duration_days": 90,
            "type": "canonical",
            "chapters": _chapters_for_books(range(39, 66)),
        },
        {
            "uuid": "built-in-psalms-proverbs",
            "title": "Psalms & Proverbs",
            "description": "Read through Psalms and Proverbs in 60 days \u2014 one month of wisdom and worship.",
            "duration_days": 60,
            "type": "topical",
            "chapters": _chapters_for_books(range(18, 20)),  # Psalms (19) + Proverbs (20)
        },
        {
            "uuid": "built-in-gospels",
            "title": "The Four Gospels",
            "description": "Walk through the life of Jesus in 30 days across Matthew, Mark, Luke, and John.",
            "duration_days": 30,
            "type": "canonical",
            "chapters": _chapters_for_books(range(39, 43)),  # Matt, Mark, Luke, John
        },
    ]

    count = 0
    for plan in plans:
        plan_uuid = plan["uuid"]
        db.execute(
            """INSERT INTO reading_plans (uuid, title, description, duration_days, type)
               VALUES (?, ?, ?, ?, ?)""",
            (plan_uuid, plan["title"], plan["description"], plan["duration_days"], plan["type"]),
        )

        # Generate daily progress entries (all start as not completed)
        daily = _build_daily_assignments(plan["chapters"], plan["duration_days"])
        for day_num, day_chapters in enumerate(daily, start=1):
            # Store chapter list as description for reference
            db.execute(
                """INSERT INTO reading_plan_progress (plan_id, day, completed, completed_at)
                   VALUES (?, ?, 0, NULL)""",
                (plan_uuid, day_num),
            )

        count += 1

    db.commit()
    print(f"      ✓ Reading plans: {count}")
