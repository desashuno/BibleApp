"""
Worship music normalizer.

Reads raw/worship/catalog.json and inserts songs, lyrics, and verse links
into the worship_* tables. Audio files are referenced by path (bundled or
user-imported).

catalog.json format:
[
  {
    "title": "Amazing Grace",
    "artist": "Public Domain",
    "album": "Classic Hymns",
    "genre": "ClassicHymns",
    "language": "en",
    "duration_ms": 240000,
    "file_path": "worship/amazing_grace.wav",
    "cover_art_path": "worship/covers/classic_hymns.jpg",
    "track_number": 1,
    "year": 1779,
    "verses": [49003006],
    "lyrics": [
      {"line_index": 0, "start_ms": 0, "end_ms": 4000, "text": "Amazing grace..."}
    ]
  }
]
"""

from __future__ import annotations

import json
import sqlite3
from pathlib import Path

RAW_DIR = Path(__file__).resolve().parent.parent / "raw"


def normalize(db: sqlite3.Connection) -> None:
    catalog_file = RAW_DIR / "worship" / "catalog.json"

    if not catalog_file.exists():
        print("  ⚠ raw/worship/catalog.json not found — skipping worship normalizer")
        return

    with open(catalog_file, encoding="utf-8") as f:
        catalog = json.load(f)

    song_count = 0
    lyric_count = 0
    verse_link_count = 0

    for entry in catalog:
        cursor = db.execute(
            """
            INSERT INTO worship_songs(
                title, artist, album, genre, language,
                duration_ms, file_path, cover_art_path,
                track_number, year, is_user_import
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """,
            (
                entry["title"],
                entry.get("artist", ""),
                entry.get("album", ""),
                entry.get("genre", ""),
                entry.get("language", "en"),
                entry.get("duration_ms", 0),
                entry.get("file_path", ""),
                entry.get("cover_art_path", ""),
                entry.get("track_number", 0),
                entry.get("year", 0),
            ),
        )
        song_id = cursor.lastrowid
        song_count += 1

        # Lyrics
        lyrics = entry.get("lyrics", [])
        if lyrics:
            full_text = "\n".join(line.get("text", "") for line in lyrics)
            db.execute(
                "INSERT OR REPLACE INTO worship_lyrics(song_id, full_text) VALUES (?, ?)",
                (song_id, full_text),
            )
            for line in lyrics:
                db.execute(
                    """
                    INSERT INTO worship_lyric_lines(
                        song_id, line_index, start_ms, end_ms, text
                    ) VALUES (?, ?, ?, ?, ?)
                    """,
                    (
                        song_id,
                        line.get("line_index", 0),
                        line.get("start_ms", 0),
                        line.get("end_ms", 0),
                        line.get("text", ""),
                    ),
                )
                lyric_count += 1

        # Verse links
        for verse_id in entry.get("verses", []):
            db.execute(
                "INSERT INTO worship_verse_links(song_id, global_verse_id) VALUES (?, ?)",
                (song_id, verse_id),
            )
            verse_link_count += 1

    db.commit()
    print(f"  ✓ {song_count} songs, {lyric_count} lyric lines, {verse_link_count} verse links")
