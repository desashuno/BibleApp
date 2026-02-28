"""
beblia_xml.py — Normalizes Bible text from Beblia/Holy-Bible-XML-Format XML files.

Source: https://github.com/Beblia/Holy-Bible-XML-Format
Only processes files with Public Domain or free license (CC BY-SA, etc.)

XML format:
    <bible translation="..." status="Public Domain">
      <testament name="Old|New">
        <book number="1..66">
          <chapter number="N">
            <verse number="N">text</verse>
          </chapter>
        </book>
      </testament>
    </bible>

Output tables: bibles, books, chapters, verses
"""

from __future__ import annotations

import sqlite3
import xml.etree.ElementTree as ET
from pathlib import Path

# Map from Beblia local filename → (abbreviation, display_name, language, direction)
BEBLIA_TRANSLATIONS = {
    "beblia-Spanish1569.xml": ("SE1569", "Sagradas Escrituras 1569", "es", "ltr"),
    "beblia-SpanishRV1909.xml": ("RV1909", "Reina-Valera 1909", "es", "ltr"),
    "beblia-SpanishRVES.xml": ("RVES", "Reina-Valera Española", "es", "ltr"),
    "beblia-SpanishVBL2022.xml": ("VBL", "Versión Biblia Libre 2022", "es", "ltr"),
}

# Book names in Spanish for Spanish translations
BOOKS_ES = [
    (1, "Génesis", "OT"), (2, "Éxodo", "OT"), (3, "Levítico", "OT"),
    (4, "Números", "OT"), (5, "Deuteronomio", "OT"), (6, "Josué", "OT"),
    (7, "Jueces", "OT"), (8, "Rut", "OT"), (9, "1 Samuel", "OT"),
    (10, "2 Samuel", "OT"), (11, "1 Reyes", "OT"), (12, "2 Reyes", "OT"),
    (13, "1 Crónicas", "OT"), (14, "2 Crónicas", "OT"), (15, "Esdras", "OT"),
    (16, "Nehemías", "OT"), (17, "Ester", "OT"), (18, "Job", "OT"),
    (19, "Salmos", "OT"), (20, "Proverbios", "OT"), (21, "Eclesiastés", "OT"),
    (22, "Cantares", "OT"), (23, "Isaías", "OT"), (24, "Jeremías", "OT"),
    (25, "Lamentaciones", "OT"), (26, "Ezequiel", "OT"), (27, "Daniel", "OT"),
    (28, "Oseas", "OT"), (29, "Joel", "OT"), (30, "Amós", "OT"),
    (31, "Abdías", "OT"), (32, "Jonás", "OT"), (33, "Miqueas", "OT"),
    (34, "Nahúm", "OT"), (35, "Habacuc", "OT"), (36, "Sofonías", "OT"),
    (37, "Hageo", "OT"), (38, "Zacarías", "OT"), (39, "Malaquías", "OT"),
    (40, "Mateo", "NT"), (41, "Marcos", "NT"), (42, "Lucas", "NT"),
    (43, "Juan", "NT"), (44, "Hechos", "NT"), (45, "Romanos", "NT"),
    (46, "1 Corintios", "NT"), (47, "2 Corintios", "NT"), (48, "Gálatas", "NT"),
    (49, "Efesios", "NT"), (50, "Filipenses", "NT"), (51, "Colosenses", "NT"),
    (52, "1 Tesalonicenses", "NT"), (53, "2 Tesalonicenses", "NT"), (54, "1 Timoteo", "NT"),
    (55, "2 Timoteo", "NT"), (56, "Tito", "NT"), (57, "Filemón", "NT"),
    (58, "Hebreos", "NT"), (59, "Santiago", "NT"), (60, "1 Pedro", "NT"),
    (61, "2 Pedro", "NT"), (62, "1 Juan", "NT"), (63, "2 Juan", "NT"),
    (64, "3 Juan", "NT"), (65, "Judas", "NT"), (66, "Apocalipsis", "NT"),
]


def compute_global_verse_id(book_number: int, chapter: int, verse: int) -> int:
    """Compute BBCCCVVV global verse ID."""
    return book_number * 1_000_000 + chapter * 1_000 + verse


def _parse_beblia_xml(xml_path: Path) -> list[tuple[int, int, int, str]]:
    """Parse a Beblia XML file and return list of (book, chapter, verse, text) tuples."""
    tree = ET.parse(xml_path)
    root = tree.getroot()

    verses: list[tuple[int, int, int, str]] = []

    for testament in root.findall("testament"):
        for book in testament.findall("book"):
            book_num = int(book.get("number", "0"))
            if book_num < 1 or book_num > 66:
                continue

            for chapter in book.findall("chapter"):
                chap_num = int(chapter.get("number", "0"))
                if chap_num < 1:
                    continue

                for verse in chapter.findall("verse"):
                    verse_num = int(verse.get("number", "0"))
                    if verse_num < 1:
                        continue

                    text = (verse.text or "").strip()
                    if text:
                        verses.append((book_num, chap_num, verse_num, text))

    return verses


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Read Beblia XML files from raw/bibles/ and write normalized Bible text."""
    bibles_dir = raw_dir / "bibles"

    for filename, (abbr, name, lang, direction) in BEBLIA_TRANSLATIONS.items():
        xml_path = bibles_dir / filename
        if not xml_path.exists():
            print(f"    ⚠ {filename} not found, skipping {abbr}")
            continue

        # Check if this translation already exists (avoid duplicates)
        existing = db.execute(
            "SELECT id FROM bibles WHERE abbreviation = ?", (abbr,)
        ).fetchone()
        if existing:
            print(f"    ✓ {abbr} already imported, skipping")
            continue

        print(f"    → Importing {abbr} ({name}) from Beblia XML")

        # Parse the XML
        rows = _parse_beblia_xml(xml_path)
        if not rows:
            print(f"    ⚠ No verses found in {filename}, skipping")
            continue

        # Use Spanish book names for Spanish translations
        books = BOOKS_ES if lang == "es" else BOOKS_ES

        # Insert Bible metadata
        cursor = db.execute(
            "INSERT INTO bibles (abbreviation, name, language, text_direction) VALUES (?, ?, ?, ?)",
            (abbr, name, lang, direction),
        )
        bible_id = cursor.lastrowid

        # Insert books
        book_id_map: dict[int, int] = {}
        for book_number, book_name, testament in books:
            cur = db.execute(
                "INSERT INTO books (bible_id, book_number, name, testament) VALUES (?, ?, ?, ?)",
                (bible_id, book_number, book_name, testament),
            )
            book_id_map[book_number] = cur.lastrowid

        # Count verses per chapter
        chapter_verse_counts: dict[tuple[int, int], int] = {}
        for book_num, chap_num, _, _ in rows:
            key = (book_num, chap_num)
            chapter_verse_counts[key] = chapter_verse_counts.get(key, 0) + 1

        # Insert chapters
        chapter_id_map: dict[tuple[int, int], int] = {}
        for (book_num, chap_num) in sorted(chapter_verse_counts.keys()):
            if book_num not in book_id_map:
                continue
            vc = chapter_verse_counts[(book_num, chap_num)]
            cur = db.execute(
                "INSERT INTO chapters (book_id, chapter_number, verse_count) VALUES (?, ?, ?)",
                (book_id_map[book_num], chap_num, vc),
            )
            chapter_id_map[(book_num, chap_num)] = cur.lastrowid

        # Insert verses
        for book_num, chap_num, verse_num, text in rows:
            key = (book_num, chap_num)
            if key not in chapter_id_map:
                continue

            global_id = compute_global_verse_id(book_num, chap_num, verse_num)
            db.execute(
                "INSERT INTO verses (chapter_id, global_verse_id, verse_number, text, html_text) VALUES (?, ?, ?, ?, NULL)",
                (chapter_id_map[key], global_id, verse_num, text),
            )

        db.commit()
        print(f"      ✓ {abbr}: {len(rows):,} verses imported from Beblia XML")
