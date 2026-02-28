#!/usr/bin/env python3
"""
normalize.py — Reads raw/ data and writes a normalized SQLite database.

Creates output/biblestudio-seed.db with the full BibleStudio schema,
then runs each normalizer module to populate it from downloaded raw data.

Usage:
    python normalize.py                     # normalize all domains
    python normalize.py --only bibles lexicon
    python normalize.py --output my-seed.db # custom output filename
"""

from __future__ import annotations

import argparse
import sqlite3
import sys
from pathlib import Path

from normalizers import bible_text, morphology, lexicon, cross_references, geography, entities, timeline, reading_plans

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

PIPELINE_DIR = Path(__file__).resolve().parent
RAW_DIR = PIPELINE_DIR / "raw"
OUTPUT_DIR = PIPELINE_DIR / "output"

DEFAULT_DB_NAME = "biblestudio-seed.db"

# ---------------------------------------------------------------------------
# Schema — mirrors BibleStudio's SQLDelight schema (DATA_LAYER.md)
# ---------------------------------------------------------------------------

SCHEMA_SQL = """
-- ===== Bible Text =====

CREATE TABLE IF NOT EXISTS bibles (
    id             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    abbreviation   TEXT    NOT NULL,
    name           TEXT    NOT NULL,
    language       TEXT    NOT NULL DEFAULT 'en',
    text_direction TEXT    NOT NULL DEFAULT 'ltr'
);

CREATE TABLE IF NOT EXISTS books (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bible_id    INTEGER NOT NULL REFERENCES bibles(id),
    book_number INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    testament   TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS chapters (
    id             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    book_id        INTEGER NOT NULL REFERENCES books(id),
    chapter_number INTEGER NOT NULL,
    verse_count    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS verses (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    chapter_id      INTEGER NOT NULL REFERENCES chapters(id),
    global_verse_id INTEGER NOT NULL,
    verse_number    INTEGER NOT NULL,
    text            TEXT    NOT NULL,
    html_text       TEXT
);

CREATE INDEX IF NOT EXISTS idx_verses_global  ON verses(global_verse_id);
CREATE INDEX IF NOT EXISTS idx_verses_chapter ON verses(chapter_id, verse_number);

-- FTS5 for Bible text search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_verses USING fts5(text, content=verses, content_rowid=id);

CREATE TRIGGER IF NOT EXISTS fts_verses_ai AFTER INSERT ON verses BEGIN
    INSERT INTO fts_verses(rowid, text) VALUES (new.id, new.text);
END;

-- ===== Annotations =====

CREATE TABLE IF NOT EXISTS notes (
    uuid            TEXT NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    title           TEXT NOT NULL DEFAULT '',
    content         TEXT NOT NULL DEFAULT '',
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now')),
    is_deleted      INTEGER NOT NULL DEFAULT 0
);

-- FTS5 for notes search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_notes USING fts5(
    title, content,
    content=notes, content_rowid=rowid
);

CREATE TABLE IF NOT EXISTS highlights (
    uuid            TEXT NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    color_index     INTEGER NOT NULL DEFAULT 0,
    style           TEXT NOT NULL DEFAULT 'background',
    is_deleted      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bookmarks (
    uuid            TEXT NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    label           TEXT NOT NULL DEFAULT '',
    folder_id       TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_deleted      INTEGER NOT NULL DEFAULT 0
);

-- ===== Study =====

CREATE TABLE IF NOT EXISTS lexicon_entries (
    strongs_number  TEXT NOT NULL PRIMARY KEY,
    original_word   TEXT NOT NULL,
    transliteration TEXT NOT NULL,
    definition      TEXT NOT NULL,
    usage_notes     TEXT
);

CREATE TABLE IF NOT EXISTS morphology (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    global_verse_id INTEGER NOT NULL,
    word_position   INTEGER NOT NULL,
    strongs_number  TEXT    NOT NULL,
    parsing_code    TEXT    NOT NULL DEFAULT '',
    surface_form    TEXT    NOT NULL DEFAULT '',
    lemma           TEXT    NOT NULL DEFAULT '',
    gloss           TEXT    NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_morphology_verse   ON morphology(global_verse_id);
CREATE INDEX IF NOT EXISTS idx_morphology_strongs ON morphology(strongs_number);

CREATE TABLE IF NOT EXISTS word_occurrences (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    strongs_number  TEXT    NOT NULL,
    global_verse_id INTEGER NOT NULL,
    word_position   INTEGER NOT NULL
);

-- FTS5 for lexicon search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_lexicon USING fts5(
    definition, usage_notes,
    content=lexicon_entries, content_rowid=rowid
);

-- ===== Resources =====

CREATE TABLE IF NOT EXISTS resources (
    uuid    TEXT NOT NULL PRIMARY KEY,
    type    TEXT NOT NULL,
    title   TEXT NOT NULL,
    author  TEXT,
    version TEXT,
    format  TEXT
);

CREATE TABLE IF NOT EXISTS resource_entries (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    resource_id     TEXT    NOT NULL REFERENCES resources(uuid),
    global_verse_id INTEGER NOT NULL,
    content         TEXT    NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0
);

-- FTS5 for resource search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_resources USING fts5(
    content,
    content=resource_entries, content_rowid=id
);

-- ===== Writing =====

CREATE TABLE IF NOT EXISTS sermons (
    uuid          TEXT NOT NULL PRIMARY KEY,
    title         TEXT NOT NULL DEFAULT '',
    scripture_ref TEXT,
    created_at    TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at    TEXT NOT NULL DEFAULT (datetime('now')),
    status        TEXT NOT NULL DEFAULT 'draft',
    is_deleted    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sermon_sections (
    id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    sermon_id  TEXT    NOT NULL REFERENCES sermons(uuid),
    type       TEXT    NOT NULL DEFAULT 'paragraph',
    content    TEXT    NOT NULL DEFAULT '',
    sort_order INTEGER NOT NULL DEFAULT 0
);

-- FTS5 for sermon search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_sermons USING fts5(
    content,
    content=sermon_sections, content_rowid=id
);

-- ===== References =====

CREATE TABLE IF NOT EXISTS cross_references (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_verse_id INTEGER NOT NULL,
    target_verse_id INTEGER NOT NULL,
    type            TEXT    NOT NULL DEFAULT 'related',
    confidence      REAL    NOT NULL DEFAULT 1.0
);

CREATE INDEX IF NOT EXISTS idx_crossref_source ON cross_references(source_verse_id);
CREATE INDEX IF NOT EXISTS idx_crossref_target ON cross_references(target_verse_id);

CREATE TABLE IF NOT EXISTS parallel_passages (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    group_id        INTEGER NOT NULL,
    global_verse_id INTEGER NOT NULL,
    label           TEXT    NOT NULL
);

-- ===== Settings =====

CREATE TABLE IF NOT EXISTS settings (
    key      TEXT NOT NULL PRIMARY KEY,
    value    TEXT NOT NULL,
    type     TEXT NOT NULL DEFAULT 'string',
    category TEXT NOT NULL DEFAULT 'general'
);

CREATE TABLE IF NOT EXISTS workspaces (
    uuid       TEXT NOT NULL PRIMARY KEY,
    name       TEXT NOT NULL,
    is_active  INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS workspace_layouts (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    workspace_id TEXT    NOT NULL REFERENCES workspaces(uuid),
    layout_json  TEXT    NOT NULL DEFAULT '{}',
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- ===== Search =====

CREATE TABLE IF NOT EXISTS search_history (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    query        TEXT NOT NULL,
    scope        TEXT NOT NULL DEFAULT 'all',
    result_count INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT NOT NULL DEFAULT (datetime('now'))
);

-- ===== Sync =====

CREATE TABLE IF NOT EXISTS sync_log (
    id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    row_id     TEXT NOT NULL,
    operation  TEXT NOT NULL,
    timestamp  TEXT NOT NULL DEFAULT (datetime('now')),
    device_id  TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS delete_log (
    id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    row_uuid   TEXT NOT NULL,
    deleted_at TEXT NOT NULL DEFAULT (datetime('now')),
    device_id  TEXT NOT NULL DEFAULT ''
);

-- ===== Timeline & Geography =====

CREATE TABLE IF NOT EXISTS timeline_events (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title           TEXT    NOT NULL,
    description     TEXT,
    year_start      INTEGER NOT NULL,
    year_end        INTEGER,
    era             TEXT    NOT NULL,
    global_verse_id INTEGER
);

CREATE INDEX IF NOT EXISTS idx_timeline_era   ON timeline_events(era);
CREATE INDEX IF NOT EXISTS idx_timeline_verse ON timeline_events(global_verse_id);
CREATE INDEX IF NOT EXISTS idx_timeline_years ON timeline_events(year_start, year_end);

CREATE TABLE IF NOT EXISTS geographic_locations (
    id               INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name             TEXT    NOT NULL,
    modern_name      TEXT,
    lat              REAL    NOT NULL,
    lon              REAL    NOT NULL,
    description      TEXT,
    verse_references TEXT    NOT NULL DEFAULT '[]'
);

CREATE INDEX IF NOT EXISTS idx_locations_name ON geographic_locations(name);

CREATE TABLE IF NOT EXISTS location_verse_index (
    location_id     INTEGER NOT NULL REFERENCES geographic_locations(id),
    global_verse_id INTEGER NOT NULL,
    PRIMARY KEY (location_id, global_verse_id)
);

CREATE INDEX IF NOT EXISTS idx_location_verse ON location_verse_index(global_verse_id);

-- ===== Knowledge Graph =====

CREATE TABLE IF NOT EXISTS entities (
    id               INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name             TEXT    NOT NULL,
    type             TEXT    NOT NULL,
    description      TEXT,
    aliases          TEXT    NOT NULL DEFAULT '[]',
    verse_references TEXT    NOT NULL DEFAULT '[]'
);

CREATE INDEX IF NOT EXISTS idx_entities_type ON entities(type);

CREATE TABLE IF NOT EXISTS relationships (
    id               INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_entity_id INTEGER NOT NULL REFERENCES entities(id),
    target_entity_id INTEGER NOT NULL REFERENCES entities(id),
    type             TEXT    NOT NULL,
    description      TEXT
);

CREATE INDEX IF NOT EXISTS idx_relationships_source ON relationships(source_entity_id);
CREATE INDEX IF NOT EXISTS idx_relationships_target ON relationships(target_entity_id);

CREATE TABLE IF NOT EXISTS entity_verse_index (
    entity_id       INTEGER NOT NULL REFERENCES entities(id),
    global_verse_id INTEGER NOT NULL,
    PRIMARY KEY (entity_id, global_verse_id)
);

CREATE INDEX IF NOT EXISTS idx_entity_verse ON entity_verse_index(global_verse_id);

-- FTS5 for entity search
CREATE VIRTUAL TABLE IF NOT EXISTS fts_entities USING fts5(
    name, description,
    content=entities, content_rowid=id
);

-- ===== Audio =====

CREATE TABLE IF NOT EXISTS audio_timestamps (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bible_id        INTEGER NOT NULL,
    global_verse_id INTEGER NOT NULL,
    start_ms        INTEGER NOT NULL,
    end_ms          INTEGER NOT NULL,
    audio_file      TEXT    NOT NULL
);

-- ===== Reading Plans =====

CREATE TABLE IF NOT EXISTS reading_plans (
    uuid          TEXT NOT NULL PRIMARY KEY,
    title         TEXT NOT NULL,
    description   TEXT,
    duration_days INTEGER NOT NULL,
    type          TEXT NOT NULL DEFAULT 'whole_bible'
);

CREATE TABLE IF NOT EXISTS reading_plan_progress (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    plan_id      TEXT    NOT NULL REFERENCES reading_plans(uuid),
    day          INTEGER NOT NULL,
    completed    INTEGER NOT NULL DEFAULT 0,
    completed_at TEXT
);

-- ===== Modules =====

CREATE TABLE IF NOT EXISTS modules (
    uuid        TEXT NOT NULL PRIMARY KEY,
    name        TEXT NOT NULL,
    abbreviation TEXT NOT NULL,
    language    TEXT NOT NULL DEFAULT 'en',
    type        TEXT NOT NULL DEFAULT 'bible',
    version     TEXT NOT NULL DEFAULT '1.0',
    size_bytes  INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    source_type TEXT NOT NULL DEFAULT 'unknown',
    installed_at TEXT NOT NULL DEFAULT (datetime('now')),
    is_deleted  INTEGER NOT NULL DEFAULT 0
);
"""


# ---------------------------------------------------------------------------
# Normalizer registry
# ---------------------------------------------------------------------------

NORMALIZER_MAP = {
    "bibles": ("Bible Text", bible_text),
    "morphology": ("Morphology (Hebrew & Greek)", morphology),
    "lexicon": ("Lexicon / Word Study", lexicon),
    "cross-references": ("Cross-References", cross_references),
    "geography": ("Geography (Atlas)", geography),
    "entities": ("Entities (Knowledge Graph)", entities),
    "timeline": ("Timeline Events", timeline),
    "reading-plans": ("Reading Plans", reading_plans),
}


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="Normalize raw Bible data into SQLite")
    parser.add_argument(
        "--only",
        nargs="+",
        choices=list(NORMALIZER_MAP.keys()),
        help="Normalize only specific data domains",
    )
    parser.add_argument(
        "--output",
        default=DEFAULT_DB_NAME,
        help=f"Output database filename (default: {DEFAULT_DB_NAME})",
    )
    parser.add_argument(
        "--verify",
        action="store_true",
        help="Verify an existing seed database meets minimum row-count thresholds",
    )
    args = parser.parse_args()

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    db_path = OUTPUT_DIR / args.output

    if args.verify:
        verify_seed_database(db_path)
        return

    # Remove existing database to start fresh
    if db_path.exists():
        db_path.unlink()
        print(f"Removed existing {db_path.name}")

    print("=" * 60)
    print("BibleStudio Data Pipeline — Normalizer")
    print(f"Output: {db_path}")
    print("=" * 60)

    # Create database with full schema
    db = sqlite3.connect(str(db_path))
    db.execute("PRAGMA journal_mode=WAL")
    db.execute("PRAGMA synchronous=NORMAL")
    db.execute("PRAGMA foreign_keys=ON")
    db.executescript(SCHEMA_SQL)
    db.commit()
    print("\n✓ Schema created (27 tables + 5 FTS5 virtual tables)")

    # Run normalizers
    targets = args.only if args.only else list(NORMALIZER_MAP.keys())

    for key in targets:
        label, module = NORMALIZER_MAP[key]
        print(f"\n{'─' * 50}")
        print(f"  {label}")
        print(f"{'─' * 50}")

        try:
            module.normalize(RAW_DIR, db)
        except Exception as e:
            print(f"\n  ✗ Error normalizing {key}: {e}", file=sys.stderr)
            import traceback
            traceback.print_exc()
            continue

    # Rebuild FTS5 indexes from populated tables
    print(f"\n{'─' * 50}")
    print("  Rebuilding FTS5 indexes")
    print(f"{'─' * 50}")
    fts_rebuilds = [
        ("fts_verses", "INSERT INTO fts_verses(fts_verses) VALUES('rebuild')"),
        ("fts_lexicon", "INSERT INTO fts_lexicon(fts_lexicon) VALUES('rebuild')"),
        ("fts_notes", "INSERT INTO fts_notes(fts_notes) VALUES('rebuild')"),
        ("fts_resources", "INSERT INTO fts_resources(fts_resources) VALUES('rebuild')"),
        ("fts_entities", "INSERT INTO fts_entities(fts_entities) VALUES('rebuild')"),
    ]
    for fts_name, rebuild_sql in fts_rebuilds:
        try:
            db.execute(rebuild_sql)
            db.commit()
            count = db.execute(f"SELECT COUNT(*) FROM {fts_name}").fetchone()[0]
            if count > 0:
                print(f"    ✓ {fts_name}: {count:,} rows indexed")
            else:
                print(f"    · {fts_name}: empty (no source data)")
        except Exception as e:
            print(f"    ⚠ {fts_name}: {e}")

    # Final stats
    print(f"\n{'=' * 60}")
    print("Summary")
    print(f"{'=' * 60}")

    stats_queries = [
        ("bibles", "SELECT COUNT(*) FROM bibles"),
        ("books", "SELECT COUNT(*) FROM books"),
        ("chapters", "SELECT COUNT(*) FROM chapters"),
        ("verses", "SELECT COUNT(*) FROM verses"),
        ("lexicon_entries", "SELECT COUNT(*) FROM lexicon_entries"),
        ("morphology", "SELECT COUNT(*) FROM morphology"),
        ("word_occurrences", "SELECT COUNT(*) FROM word_occurrences"),
        ("cross_references", "SELECT COUNT(*) FROM cross_references"),
        ("parallel_passages", "SELECT COUNT(*) FROM parallel_passages"),
        ("geographic_locations", "SELECT COUNT(*) FROM geographic_locations"),
        ("entities", "SELECT COUNT(*) FROM entities"),
        ("relationships", "SELECT COUNT(*) FROM relationships"),
        ("timeline_events", "SELECT COUNT(*) FROM timeline_events"),
        ("reading_plans", "SELECT COUNT(*) FROM reading_plans"),
    ]

    for table, query in stats_queries:
        try:
            count = db.execute(query).fetchone()[0]
            if count > 0:
                print(f"  {table:.<30} {count:>10,}")
        except Exception:
            pass

    # Database size
    db.close()
    size_mb = db_path.stat().st_size / (1024 * 1024)
    print(f"\n  Database size: {size_mb:.1f} MB")
    print(f"  Location: {db_path}")
    print(f"\n{'=' * 60}")
    print("Done. Copy the seed database to the app's assets or data directory.")
    print(f"{'=' * 60}")


# ---------------------------------------------------------------------------
# Verification
# ---------------------------------------------------------------------------

MIN_ROW_COUNTS = {
    "bibles": 3,
    "books": 60,
    "verses": 150_000,
    "lexicon_entries": 10_000,
    "morphology": 100_000,
    "word_occurrences": 200_000,
    "cross_references": 300_000,
    "geographic_locations": 1_000,
    "entities": 10_000,
    "relationships": 3_000,
    "timeline_events": 50,
    "reading_plans": 4,
}


def verify_seed_database(db_path: Path) -> None:
    """Check that the seed database meets minimum row-count thresholds."""
    if not db_path.exists():
        print(f"ERROR: Seed database not found at {db_path}", file=sys.stderr)
        sys.exit(1)

    db = sqlite3.connect(str(db_path))
    print("=" * 60)
    print("BibleStudio Seed Database — Verification")
    print(f"Database: {db_path}")
    print("=" * 60)

    failures = []
    for table, minimum in MIN_ROW_COUNTS.items():
        try:
            count = db.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]
            status = "✓" if count >= minimum else "✗"
            if count < minimum:
                failures.append((table, minimum, count))
            print(f"  {status} {table:.<30} {count:>10,}  (min: {minimum:,})")
        except Exception as e:
            failures.append((table, minimum, 0))
            print(f"  ✗ {table:.<30} ERROR: {e}")

    # Check FTS5 tables
    for fts_table in ["fts_verses", "fts_lexicon", "fts_entities"]:
        try:
            count = db.execute(f"SELECT COUNT(*) FROM {fts_table}").fetchone()[0]
            status = "✓" if count > 0 else "·"
            print(f"  {status} {fts_table:.<30} {count:>10,}")
        except Exception as e:
            print(f"  · {fts_table:.<30} ERROR: {e}")

    db.close()
    size_mb = db_path.stat().st_size / (1024 * 1024)
    print(f"\n  Database size: {size_mb:.1f} MB")

    if failures:
        print(f"\nFAILED: {len(failures)} table(s) below minimum:")
        for table, minimum, actual in failures:
            print(f"  {table}: expected >= {minimum:,}, got {actual:,}")
        sys.exit(1)
    else:
        print(f"\n✓ All {len(MIN_ROW_COUNTS)} tables pass minimum row-count checks.")


if __name__ == "__main__":
    main()
