# Phase 3 — Core Modules

> Bible Reader, Search, Cross-References, Settings, Module System, Import/Export.
> **Prerequisites**: Phase 2 complete (VerseBus, PaneRegistry, WorkspaceShell, AppTheme).

---

## 3.1 Bible Reader — Data Layer

- [ ] Implement `BibleRepository` with `getBooks()`, `getChapters(bookId)`, `getVerses(bookId, chapter)`
- [ ] Implement `getVerse(globalVerseId: Long): Verse`
- [ ] Map `verses` table columns to `Verse` data class
- [ ] Map `books` table to `Book` data class (abbreviation, testament, order)
- [ ] Create `BibleTextQuery.sq` with parameterized queries
- [ ] Implement verse-range query `getVerseRange(startId, endId): List<Verse>`
- [ ] Register repository in Koin `repositoryModule`
- [ ] Write test: getVerses returns correct chapter content
- [ ] Write test: getVerse returns single verse by global ID

## 3.2 Bible Reader — Component & UI

- [ ] Create `BibleReaderComponent` with Decompose: `StateFlow<BibleReaderState>`
- [ ] Define `BibleReaderState`: `currentBook`, `currentChapter`, `verses`, `scrollPosition`, `isLoading`
- [ ] Implement chapter navigation: `goToChapter(bookId, chapter)`
- [ ] Implement verse tap: publish `LinkEvent.VerseSelected` to VerseBus
- [ ] Subscribe to incoming `LinkEvent.VerseSelected` — scroll to verse
- [ ] Subscribe to incoming `LinkEvent.PassageSelected` — load passage range
- [ ] Create `BibleReaderPane` composable: verse list with `LazyColumn`
- [ ] Style verse numbers (superscript, accent color), verse text, paragraph breaks
- [ ] Implement book/chapter picker bottom sheet
- [ ] Implement long-press verse selection (highlight selection range)
- [ ] Register `bibleReader` in `PaneRegistry`
- [ ] Write UI test: BibleReaderPane renders verses
- [ ] Write UI test: verse tap publishes VerseBus event

## 3.3 Text Comparison

- [ ] Create `TextComparisonComponent` with multiple translation state
- [ ] Implement parallel view: side-by-side columns per translation
- [ ] Implement interleaved view: verse-by-verse alternating translations
- [ ] Implement difference highlighting between translations (word-level diff)
- [ ] Load verse content from multiple module databases
- [ ] Register `textComparison` in `PaneRegistry`
- [ ] Write UI test: parallel view shows 2+ columns
- [ ] Write test: word-level diff algorithm highlights changes

## 3.4 Search — Data Layer

- [ ] Create `SearchRepository` with `searchFts(query: String, modules: List<String>): List<SearchResult>`
- [ ] Implement FTS5 query builder: `MATCH` syntax with `AND`, `OR`, `NOT`, `NEAR`
- [ ] Implement `SearchResult` data class: `verseId`, `bookName`, `chapter`, `verseNum`, `snippet`, `rank`
- [ ] Implement search filters: book range, testament, module selection
- [ ] Create `SearchQuery.sq` with FTS5 rank-ordered queries
- [ ] Register repository in Koin
- [ ] Write test: FTS5 query returns ranked results
- [ ] Write test: book range filter narrows results correctly

## 3.5 Search — Component & UI

- [ ] Create `SearchComponent` with `StateFlow<SearchState>`
- [ ] Define `SearchState`: `query`, `results`, `isSearching`, `filters`, `resultCount`
- [ ] Implement debounced search (300ms delay)
- [ ] Implement result tap: publish `LinkEvent.SearchResult` to VerseBus
- [ ] Create `SearchPane` composable: search bar + result `LazyColumn`
- [ ] Implement result snippet with highlighted match terms
- [ ] Implement search history (last 20 queries from `search_history` table)
- [ ] Register `search` in `PaneRegistry`
- [ ] Write UI test: search results display correctly
- [ ] Write test: debounce waits 300ms before searching

## 3.6 Syntax Search

- [ ] Implement syntax search grammar: `[LEMMA:H1234]`, `[POS:Noun]`, `[WITHIN 3 WORDS]`
- [ ] Create syntax search query parser (tokenizer → AST → SQL query builder)
- [ ] Combine morphology + FTS5 queries for syntax search
- [ ] Create `SyntaxSearchPane` composable with syntax help popover
- [ ] Register `syntaxSearch` in `PaneRegistry`
- [ ] Write test: syntax parser produces correct AST
- [ ] Write test: syntax search returns morphologically filtered results

## 3.7 Cross-References — Data Layer

- [ ] Create `CrossReferenceRepository` with `getReferences(verseId: Long): List<CrossReference>`
- [ ] Map `cross_references` table to `CrossReference` data class: `sourceVerseId`, `targetVerseId`, `type`, `confidence`
- [ ] Load TSK (Treasury of Scripture Knowledge) data into table
- [ ] Create `CrossReferenceQuery.sq` with join to `verses` for target text preview
- [ ] Register repository in Koin
- [ ] Write test: cross-reference returns correct links for a verse

## 3.8 Cross-References — Component & UI

- [ ] Create `CrossReferenceComponent` subscribing to VerseBus `VerseSelected`
- [ ] Define `CrossReferenceState`: `sourceVerse`, `references`, `isLoading`
- [ ] Implement reference tap: publish `LinkEvent.VerseSelected` for target verse
- [ ] Create `CrossReferencePane` composable: reference list with verse preview snippets
- [ ] Implement inline expansion: tap to reveal full target verse text
- [ ] Implement reference type badges (parallel, quotation, allusion)
- [ ] Register `crossReferences` in `PaneRegistry`
- [ ] Write UI test: reference list renders for a verse
- [ ] Write test: reference tap publishes VerseBus event

## 3.9 Settings

- [ ] Create `SettingsRepository` with `getString(key)`, `setString(key, value)`, `getInt()`, `setInt()`, `getBoolean()`, `setBoolean()`
- [ ] Back all settings by `settings` table key-value store
- [ ] Create `SettingsComponent` with `StateFlow<SettingsState>`
- [ ] Define `SettingsState` with grouped preference sections
- [ ] Implement font size setting (12–28sp range, applied globally)
- [ ] Implement theme selection (System / Light / Dark)
- [ ] Implement default bible module selection
- [ ] Implement workspace layout management (save/load/rename/delete layouts)
- [ ] Create `SettingsScreen` composable: grouped list of preference tiles
- [ ] Register as Decompose `Settings` config screen
- [ ] Write test: settings persist and survive app restart
- [ ] Write test: font size change propagates to theme

## 3.10 Module System — Data Layer

- [ ] Create `ModuleRepository` with `getInstalledModules(): List<InstalledModule>`
- [ ] Map `modules` table to `InstalledModule` data class: `id`, `name`, `abbreviation`, `language`, `type`, `version`, `sizeBytes`
- [ ] Implement `installModule(source: ModuleSource)` — parse, validate, insert into DB
- [ ] Implement `removeModule(moduleId: String)` — cascade delete
- [ ] Implement `ModuleSource` sealed class: `Sword`, `OSIS`, `USFM`, `CustomZip`
- [ ] Create `ModuleQuery.sq` for module metadata CRUD
- [ ] Register repository in Koin
- [ ] Write test: install + list round-trip
- [ ] Write test: remove cascades dependent data

## 3.11 Module System — OSIS & Sword Parsers

- [ ] Implement OSIS XML parser: `<verse>`, `<chapter>`, `<note>`, `<reference>`, `<w lemma="">`
- [ ] Implement USFM parser: `\v`, `\c`, `\p`, `\f`, `\x`, `\w ... \w*`
- [ ] Implement Sword module reader (`.conf` + compressed data)
- [ ] Map parsed data to `Verse` + `Morphology` + `CrossReference` inserts
- [ ] Validate module integrity: required books, verse mappings, character encoding
- [ ] Create progress callback for UI progress bars
- [ ] Write test: OSIS sample parses correctly
- [ ] Write test: USFM sample parses correctly
- [ ] Write test: invalid module produces validation errors

## 3.12 Module System — UI

- [ ] Create `ModuleManagerComponent` with `StateFlow<ModuleManagerState>`
- [ ] Define `ModuleManagerState`: `installedModules`, `availableModules`, `downloadProgress`
- [ ] Create `ModuleManagerPane` composable: module list with install/remove actions
- [ ] Implement module detail card: name, language, type, size, version, description
- [ ] Implement import from file: platform file picker integration
- [ ] Register `moduleManager` in `PaneRegistry`
- [ ] Write UI test: module list renders installed modules

## 3.13 Import/Export

- [ ] Create `ImportExportRepository` with `exportNotes(format)`, `importNotes(file)`, `exportHighlights()`, `importHighlights()`
- [ ] Implement JSON export format for notes, highlights, bookmarks
- [ ] Implement CSV export for reading plans progress
- [ ] Implement backup bundle: SQLite DB + module metadata zip
- [ ] Implement restore from backup bundle
- [ ] Create `ImportExportScreen` composable: export/import buttons with format picker
- [ ] Register as Decompose `Import` config screen
- [ ] Write test: export → import round-trips notes correctly
- [ ] Write test: backup bundle contains all expected data

---

## Phase 3 Exit Criteria

- [ ] Bible Reader loads and displays chapter text with verse numbers
- [ ] Text Comparison shows parallel translations with diff highlighting
- [ ] FTS5 search returns ranked results with snippet highlighting
- [ ] Syntax search parses and executes morphology-aware queries
- [ ] Cross-references load and navigate between verses via VerseBus
- [ ] Settings persist and propagate (font, theme, default module)
- [ ] Module system installs OSIS, USFM, and Sword modules
- [ ] Import/Export round-trips user data (notes, highlights, bookmarks)
- [ ] All core module tests pass with ≥ 80% coverage
