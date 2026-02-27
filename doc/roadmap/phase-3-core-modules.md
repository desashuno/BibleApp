# Phase 3 — Core Modules

> Bible Reader, Search, Cross-References, Settings, Module System, Import/Export.
> **Prerequisites**: Phase 2 complete (VerseBus, PaneRegistry, WorkspaceShell, AppTheme).

---

## 3.1 Bible Reader — Data Layer

- [x] Implement `BibleRepository` with `getBooks()`, `getChapters(bookId)`, `getVerses(bookId, chapter)`
- [x] Implement `getVerse(globalVerseId: Long): Verse`
- [x] Map `verses` table columns to `Verse` data class
- [x] Map `books` table to `Book` data class (abbreviation, testament, order)
- [x] Create `BibleTextQuery.sq` with parameterized queries
- [x] Implement verse-range query `getVerseRange(startId, endId): List<Verse>`
- [x] Register repository in Koin `repositoryModule`
- [x] Write test: getVerses returns correct chapter content
- [x] Write test: getVerse returns single verse by global ID

## 3.2 Bible Reader — Component & UI

- [x] Create `BibleReaderComponent` with Decompose: `StateFlow<BibleReaderState>`
- [x] Define `BibleReaderState`: `currentBook`, `currentChapter`, `verses`, `scrollPosition`, `isLoading`
- [x] Implement chapter navigation: `goToChapter(bookId, chapter)`
- [x] Implement verse tap: publish `LinkEvent.VerseSelected` to VerseBus
- [x] Subscribe to incoming `LinkEvent.VerseSelected` — scroll to verse
- [x] Subscribe to incoming `LinkEvent.PassageSelected` — load passage range
- [x] Create `BibleReaderPane` composable: verse list with `LazyColumn`
- [x] Style verse numbers (superscript, accent color), verse text, paragraph breaks
- [x] Implement book/chapter picker bottom sheet
- [x] Implement long-press verse selection (highlight selection range)
- [x] Register `bibleReader` in `PaneRegistry`
- [x] Write UI test: BibleReaderPane renders verses
- [x] Write UI test: verse tap publishes VerseBus event

## 3.3 Text Comparison

- [x] Create `TextComparisonComponent` with multiple translation state
- [x] Implement parallel view: side-by-side columns per translation
- [x] Implement interleaved view: verse-by-verse alternating translations
- [x] Implement difference highlighting between translations (word-level diff)
- [x] Load verse content from multiple module databases
- [x] Register `textComparison` in `PaneRegistry`
- [x] Write UI test: parallel view shows 2+ columns
- [x] Write test: word-level diff algorithm highlights changes

## 3.4 Search — Data Layer

- [x] Create `SearchRepository` with `searchFts(query: String, modules: List<String>): List<SearchResult>`
- [x] Implement FTS5 query builder: `MATCH` syntax with `AND`, `OR`, `NOT`, `NEAR`
- [x] Implement `SearchResult` data class: `verseId`, `bookName`, `chapter`, `verseNum`, `snippet`, `rank`
- [x] Implement search filters: book range, testament, module selection
- [x] Create `SearchQuery.sq` with FTS5 rank-ordered queries
- [x] Register repository in Koin
- [x] Write test: FTS5 query returns ranked results
- [x] Write test: book range filter narrows results correctly

## 3.5 Search — Component & UI

- [x] Create `SearchComponent` with `StateFlow<SearchState>`
- [x] Define `SearchState`: `query`, `results`, `isSearching`, `filters`, `resultCount`
- [x] Implement debounced search (300ms delay)
- [x] Implement result tap: publish `LinkEvent.SearchResult` to VerseBus
- [x] Create `SearchPane` composable: search bar + result `LazyColumn`
- [x] Implement result snippet with highlighted match terms
- [x] Implement search history (last 20 queries from `search_history` table)
- [x] Register `search` in `PaneRegistry`
- [x] Write UI test: search results display correctly
- [x] Write test: debounce waits 300ms before searching

## 3.6 Syntax Search

- [x] Implement syntax search grammar: `[LEMMA:H1234]`, `[POS:Noun]`, `[WITHIN 3 WORDS]`
- [x] Create syntax search query parser (tokenizer → AST → SQL query builder)
- [x] Combine morphology + FTS5 queries for syntax search
- [x] Create `SyntaxSearchPane` composable with syntax help popover
- [x] Register `syntaxSearch` in `PaneRegistry`
- [x] Write test: syntax parser produces correct AST
- [x] Write test: syntax search returns morphologically filtered results

## 3.7 Cross-References — Data Layer

- [x] Create `CrossReferenceRepository` with `getReferences(verseId: Long): List<CrossReference>`
- [x] Map `cross_references` table to `CrossReference` data class: `sourceVerseId`, `targetVerseId`, `type`, `confidence`
- [x] Load TSK (Treasury of Scripture Knowledge) data into table
- [x] Create `CrossReferenceQuery.sq` with join to `verses` for target text preview
- [x] Register repository in Koin
- [x] Write test: cross-reference returns correct links for a verse

## 3.8 Cross-References — Component & UI

- [x] Create `CrossReferenceComponent` subscribing to VerseBus `VerseSelected`
- [x] Define `CrossReferenceState`: `sourceVerse`, `references`, `isLoading`
- [x] Implement reference tap: publish `LinkEvent.VerseSelected` for target verse
- [x] Create `CrossReferencePane` composable: reference list with verse preview snippets
- [x] Implement inline expansion: tap to reveal full target verse text
- [x] Implement reference type badges (parallel, quotation, allusion)
- [x] Register `crossReferences` in `PaneRegistry`
- [x] Write UI test: reference list renders for a verse
- [x] Write test: reference tap publishes VerseBus event

## 3.9 Settings

- [x] Create `SettingsRepository` with `getString(key)`, `setString(key, value)`, `getInt()`, `setInt()`, `getBoolean()`, `setBoolean()`
- [x] Back all settings by `settings` table key-value store
- [x] Create `SettingsComponent` with `StateFlow<SettingsState>`
- [x] Define `SettingsState` with grouped preference sections
- [x] Implement font size setting (12–28sp range, applied globally)
- [x] Implement theme selection (System / Light / Dark)
- [x] Implement default bible module selection
- [x] Implement workspace layout management (save/load/rename/delete layouts)
- [x] Create `SettingsScreen` composable: grouped list of preference tiles
- [x] Register as Decompose `Settings` config screen
- [x] Write test: settings persist and survive app restart
- [x] Write test: font size change propagates to theme

## 3.10 Module System — Data Layer

- [x] Create `ModuleRepository` with `getInstalledModules(): List<InstalledModule>`
- [x] Map `modules` table to `InstalledModule` data class: `id`, `name`, `abbreviation`, `language`, `type`, `version`, `sizeBytes`
- [x] Implement `installModule(source: ModuleSource)` — parse, validate, insert into DB
- [x] Implement `removeModule(moduleId: String)` — cascade delete
- [x] Implement `ModuleSource` sealed class: `Sword`, `OSIS`, `USFM`, `CustomZip`
- [x] Create `ModuleQuery.sq` for module metadata CRUD
- [x] Register repository in Koin
- [x] Write test: install + list round-trip
- [x] Write test: remove cascades dependent data

## 3.11 Module System — OSIS & Sword Parsers

- [x] Implement OSIS XML parser: `<verse>`, `<chapter>`, `<note>`, `<reference>`, `<w lemma="">`
- [x] Implement USFM parser: `\v`, `\c`, `\p`, `\f`, `\x`, `\w ... \w*`
- [x] Implement Sword module reader (`.conf` + compressed data)
- [x] Map parsed data to `Verse` + `Morphology` + `CrossReference` inserts
- [x] Validate module integrity: required books, verse mappings, character encoding
- [x] Create progress callback for UI progress bars
- [x] Write test: OSIS sample parses correctly
- [x] Write test: USFM sample parses correctly
- [x] Write test: invalid module produces validation errors

## 3.12 Module System — UI

- [x] Create `ModuleManagerComponent` with `StateFlow<ModuleManagerState>`
- [x] Define `ModuleManagerState`: `installedModules`, `availableModules`, `downloadProgress`
- [x] Create `ModuleManagerPane` composable: module list with install/remove actions
- [x] Implement module detail card: name, language, type, size, version, description
- [x] Implement import from file: platform file picker integration
- [x] Register `moduleManager` in `PaneRegistry`
- [x] Write UI test: module list renders installed modules

## 3.13 Import/Export

- [x] Create `ImportExportRepository` with `exportNotes(format)`, `importNotes(file)`, `exportHighlights()`, `importHighlights()`
- [x] Implement JSON export format for notes, highlights, bookmarks
- [x] Implement CSV export for reading plans progress
- [x] Implement backup bundle: SQLite DB + module metadata zip
- [x] Implement restore from backup bundle
- [x] Create `ImportExportScreen` composable: export/import buttons with format picker
- [x] Register as Decompose `Import` config screen
- [x] Write test: export → import round-trips notes correctly
- [x] Write test: backup bundle contains all expected data

---

## Phase 3 Exit Criteria

- [x] Bible Reader loads and displays chapter text with verse numbers
- [x] Text Comparison shows parallel translations with diff highlighting
- [x] FTS5 search returns ranked results with snippet highlighting
- [x] Syntax search parses and executes morphology-aware queries
- [x] Cross-references load and navigate between verses via VerseBus
- [x] Settings persist and propagate (font, theme, default module)
- [x] Module system installs OSIS, USFM, and Sword modules
- [x] Import/Export round-trips user data (notes, highlights, bookmarks)
- [x] All core module tests pass with ≥ 80% coverage
