# Phase 6 — Data Pipeline Integration & Module Enhancement

> Wire the open-source data pipeline into the application, replace all hardcoded/stub data
> with seed-database content, and deliver module-level enhancements.
> **Prerequisites**: Phases 3–5 modules functional; data pipeline producing `biblestudio-seed.db`.

---

## 6.1 Data Pipeline — Production Readiness

- [x] Fix `download.py`: TAHOT/TAGNT split-file concatenation, cross-refs zip extraction
- [x] Fix `normalizers/bible_text.py`: scrollmapper 2024 schema (`b`, `c`, `v`, `t` columns)
- [x] Fix `normalizers/geography.py`: OpenBible JSONL format (`friendly_id`, `identifications[].resolutions[].lonlat`)
- [x] Fix `normalizers/cross_references.py`: updated extracted filename
- [x] Add missing FTS5 virtual tables (`fts_notes`, `fts_entities`) to schema
- [x] Add FTS5 rebuild step after all normalizers run
- [x] Align `reading_plans.py` UUIDs with `BuiltInPlans.kt`
- [x] Full pipeline run: 5 Bibles, 155K verses, 427K morphology words, 19K lexicon entries, 344K cross-refs, 1,335 locations, 14K entities, 68 timeline events, 4 reading plans
- [x] Add pipeline CI step: `python download.py && python normalize.py` in GitHub Actions
- [ ] Add SHA-256 checksum verification for downloaded raw files
- [x] Add `--verify` flag to `normalize.py` for post-build integrity checks

## 6.2 Seed Database Bundling

- [x] Create `copySeedDatabaseIfNeeded()` expect/actual for Desktop, Android, iOS
- [x] Implement first-launch database copy: detect empty DB → copy seed → skip schema.create
- [ ] Add database version check: compare seed schema version with installed DB
- [ ] Add incremental update support: apply delta patches when seed DB is updated (deferred)
- [ ] Verify seed DB loads correctly on Android, iOS, and Desktop targets

## 6.3 Module Data Wiring

### Bible Reader
- [x] Verify `BibleRepositoryImpl` reads from seed DB (5 translations: KJV, ASV, WEB, YLT, BBE)
- [x] Wire translation picker to `bibles` table
- [x] Populate `fts_verses` for full-text Bible search (FTS5 rebuild in normalize.py)

### Cross-References
- [x] Replace `loadTskData()` stub with `crossRefCount()` query
- [x] Add `crossRefCount` query to `Reference.sq`
- [x] Verify `CrossRefRepositoryImpl` returns data for any verse
- [x] Wire confidence score into reference list sorting/display (`ORDER BY confidence DESC`)

### Morphology & Interlinear
- [x] Verify `MorphologyRepositoryImpl` returns Hebrew/Greek data per verse
- [x] Ensure `ParsingDecoder` handles TAHOT/TAGNT parsing codes
- [x] Populate interlinear grid from seed DB morphology data

### Word Study & Lexicon
- [x] Verify `WordStudyRepository` returns lexicon entries by Strong's number
- [x] Wire `fts_lexicon` search into the word study search UI
- [x] Display occurrence count from `word_occurrences` table

### Knowledge Graph
- [x] Verify `KnowledgeGraphRepositoryImpl` returns entities and relationships
- [x] Wire `fts_entities` search into entity browser
- [x] Display entity-verse links from `entity_verse_index`

### Theological Atlas
- [x] Replace Canvas dot map with OpenStreetMap tile renderer (`OsmTileMap`)
- [x] Create expect/actual `loadTileBitmap` for Desktop, Android, iOS
- [x] Add zoom controls and pin-tap interaction
- [x] Verify `AtlasRepositoryImpl` returns geocoded locations from seed DB
- [x] Wire verse-aware auto-centering via VerseBus + `location_verse_index`

### Timeline
- [x] Verify `TimelineRepositoryImpl` reads events from seed DB
- [x] Display events with era grouping on timeline UI

### Reading Plans
- [x] Align `BuiltInPlans.kt` UUIDs with seed DB
- [x] Verify `seedBuiltInPlans()` skips insert when seed DB already has plans
- [x] Wire daily progress tracking to `reading_plan_progress` table

## 6.4 Module Enhancements

### Atlas (OSM Tile Map)
- [x] Implement disk-based tile cache for offline use (`DiskTileCache` expect/actual)
- [ ] Add journey route polylines (Paul's journeys, Exodus route) — deferred P1
- [ ] Add timeline-synced era filter slider — deferred P2
- [ ] Add region boundary polygon overlays — deferred P2

### Cross-References
- [x] Add confidence heatmap visual indicator (`confidenceColor()` in `CrossReferencePane`)
- [ ] Add cross-ref graph view (network visualization) — deferred P1

### Knowledge Graph
- [x] Add entity type icons (Person, Place, Event, Concept, Book) — `entityTypeIcon()` in `KnowledgeGraphPane`
- [ ] Add family tree specialized layout for genealogical data — deferred P1
- [ ] Cross-link entities to atlas locations and timeline events — deferred P1

### Word Study
- [ ] Add word frequency bar chart by book — deferred P2
- [ ] Add word cloud visualization for chapters — deferred P2

## 6.5 Documentation Updates

- [x] Update `theological-atlas/ROADMAP.md` with OSM upgrade and enhancement ideas
- [x] Update `cross-references/ROADMAP.md` with data pipeline stats and enhancements
- [x] Update `knowledge-graph/ROADMAP.md` with entity/relationship stats and enhancements
- [x] Update `word-study/ROADMAP.md` with lexicon stats and enhancements
- [x] Update `OPEN_DATA_SOURCES.md` with pipeline output statistics table
- [x] Add data pipeline usage instructions, CI docs, and seed bundling info to `data-pipeline/README.md`

---

## Phase 6 Exit Criteria

- [x] `copySeedDatabaseIfNeeded()` implemented for all 3 platforms (bundle seed DB pending)
- [x] All repository implementations wired to seed DB via SQLDelight (no stubs/hardcoded data)
- [x] Atlas displays OSM tiles with disk cache + biblical location pins
- [x] FTS5 search works for verses, lexicon, and entities (rebuild step in normalize.py)
- [x] Cross-references sorted by confidence DESC with heatmap badge colors
- [x] Knowledge graph displays entities with type icons and relationship navigation
- [x] All module ROADMAP.md files updated with enhancement plans
- [x] Data pipeline CI workflow created (`.github/workflows/data-pipeline.yml`)
