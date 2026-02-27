# Phase 4 — Study & Writing Modules

> Word Study, Morphology/Interlinear, Reverse Interlinear, Passage Guide, Resource Library, Note Editor, Highlights, Bookmarks/History.
> **Prerequisites**: Phase 3 complete (Bible Reader, Search, Cross-References, Module System).

---

## 4.1 Word Study — Data Layer

- [x] Create `WordStudyRepository` with `getStrongs(strongsId: String): StrongsEntry`
- [x] Map `strongs` table to `StrongsEntry` data class: `id`, `lemma`, `transliteration`, `pronunciation`, `definition`, `usage`
- [x] Implement `getOccurrences(strongsId: String): List<VerseOccurrence>` — query FTS `strongs_fts` table
- [x] Implement `getRelatedWords(strongsId: String): List<StrongsEntry>` — semantic/root relations
- [x] Create `WordStudyQuery.sq`
- [x] Register repository in Koin
- [x] Write test: Strong's entry resolves from ID
- [x] Write test: occurrence count matches expected total

## 4.2 Word Study — Component & UI

- [x] Create `WordStudyComponent` subscribing to VerseBus `StrongsSelected`
- [x] Define `WordStudyState`: `entry`, `occurrences`, `relatedWords`, `isLoading`
- [x] Implement occurrence frequency chart (bar chart by book)
- [x] Create `WordStudyPane` composable: definition card + occurrence list + chart
- [x] Implement occurrence tap: publish `LinkEvent.VerseSelected`
- [x] Implement semantic domain grouping of occurrences
- [x] Register `wordStudy` in `PaneRegistry`
- [x] Write UI test: word study pane renders definition
- [x] Write test: VerseBus subscription loads correct entry

## 4.3 Morphology / Interlinear — Data Layer

- [x] Create `MorphologyRepository` with `getMorphology(verseId: Long): List<MorphWord>`
- [x] Map `morphology` table to `MorphWord` data class: `surfaceForm`, `lemma`, `strongsId`, `parsing`, `gloss`, `position`
- [x] Implement parsing decoder: `V-AAI-3S` → "Verb, Aorist, Active, Indicative, 3rd Person, Singular"
- [x] Create `MorphologyQuery.sq` ordered by word position
- [x] Register repository in Koin
- [x] Write test: morphology returns words in positional order
- [x] Write test: parsing decoder handles all POS tags

## 4.4 Morphology / Interlinear — Component & UI

- [x] Create `InterlinearComponent` subscribing to VerseBus `VerseSelected`
- [x] Define `InterlinearState`: `verse`, `words`, `displayMode`, `isLoading`
- [x] Implement display modes: `Interlinear` (stacked), `Parallel` (side-by-side), `Inline` (tooltip)
- [x] Create `InterlinearPane` composable: word grid with original + transliteration + gloss + parsing rows
- [x] Implement word tap: 4.3publish `LinkEvent.StrongsSelected` to VerseBus
- [x] Style Hebrew RTL text with proper bidirectional rendering
- [x] Style Greek text with correct polytonic diacritics
- [x] Register `interlinear` in `PaneRegistry`
- [x] Write UI test: interlinear renders 4-row word grid
- [x] Write test: word tap publishes StrongsSelected event

## 4.5 Reverse Interlinear

- [x] Create `ReverseInterlinearComponent` that maps English words back to original language
- [x] Implement alignment algorithm: match translation tokens to `morphology` entries via Strong's
- [x] Create `ReverseInterlinearPane` composable: English text with underlined linked words
- [x] Implement inline popover on tap: show original word, transliteration, parsing, definition
- [x] Support multiple translation alignments (configurable base translation)
- [x] Register `reverseInterlinear` in `PaneRegistry`
- [x] Write test: alignment correctly maps English tokens to morphology entries
- [x] Write UI test: tap on English word shows original language popover

## 4.6 Passage Guide — Data Layer

- [x] Create `PassageGuideRepository` aggregating data from multiple repositories
- [x] Implement `getGuide(verseId: Long): PassageGuide` — parallel query: cross-refs + outlines + themes + word studies
- [x] Map `outlines` table to `Outline` data class: `title`, `points`, `sourceVerseRange`
- [x] Create `PassageGuideQuery.sq` for outline and thematic queries
- [x] Register repository in Koin
- [x] Write test: passage guide aggregates all data sources

## 4.7 Passage Guide — Component & UI

- [x] Create `PassageGuideComponent` subscribing to VerseBus `VerseSelected` / `PassageSelected`
- [x] Define `PassageGuideState`: `verse`, `crossRefs`, `outlines`, `themes`, `keyWords`, `isLoading`
- [x] Create `PassageGuidePane` composable: sectioned card layout (Outline, Cross-Refs, Key Words, Themes)
- [x] Implement expandable sections with lazy loading
- [x] Implement card tap navigation: cross-ref → VerseBus, word → StrongsSelected
- [x] Register `passageGuide` in `PaneRegistry`
- [x] Write UI test: passage guide renders all sections

## 4.8 Resource Library — Data Layer

- [x] Create `ResourceRepository` with `getResources(type: ResourceType): List<Resource>`
- [x] Map `resources` table to `Resource` data class: `id`, `title`, `author`, `type`, `moduleId`
- [x] Implement `ResourceType` enum: `Commentary`, `Dictionary`, `Encyclopedia`, `DevotionalBook`
- [x] Implement `getResourceEntry(resourceId, verseId): ResourceEntry` — look up commentary/dict entry for a verse
- [x] Create `ResourceQuery.sq`
- [x] Register repository in Koin
- [x] Write test: resource entries resolve for verse range

## 4.9 Resource Library — Component & UI

- [x] Create `ResourceLibraryComponent` subscribing to VerseBus `ResourceSelected` / `VerseSelected`
- [x] Define `ResourceLibraryState`: `resources`, `activeResource`, `entry`, `isLoading`
- [x] Create `ResourceLibraryPane` composable: sidebar resource list + content area with formatted entry
- [x] Implement rich text rendering for commentary entries (bold, italic, links, footnotes)
- [x] Implement resource search: filter/search within active resource
- [x] Register `resourceLibrary` in `PaneRegistry`
- [x] Write UI test: resource list shows installed commentaries
- [x] Write test: VerseBus VerseSelected loads commentary for that verse

## 4.10 Note Editor — Data Layer

- [x] Create `NoteRepository` with `getNotes(verseId: Long): List<Note>`, `saveNote(note: Note)`, `deleteNote(noteId: Long)`
- [x] Map `notes` table to `Note` data class: `id`, `verseId`, `title`, `content`, `format`, `createdAt`, `updatedAt`
- [x] Implement `NoteFormat` enum: `PlainText`, `Markdown`, `RichText`
- [x] Implement `searchNotes(query: String): List<Note>` using `notes_fts` FTS5 table
- [x] Create `NoteQuery.sq` with pagination support
- [x] Register repository in Koin
- [x] Write test: CRUD operations on notes
- [x] Write test: FTS5 search returns matching notes

## 4.11 Note Editor — Component & UI

- [x] Create `NoteEditorComponent` with `StateFlow<NoteEditorState>`
- [x] Define `NoteEditorState`: `note`, `isDirty`, `isSaving`, `format`
- [x] Implement Markdown editor with `BasicTextField` and live preview
- [x] Implement rich text toolbar: bold, italic, heading, list, quote, link, verse reference
- [x] Implement verse reference insertion: pick verse → insert `[Gen 1:1]` link
- [x] Implement auto-save with 2-second debounce
- [x] Create note list view with search and sort (date, title, verse)
- [x] Register `noteEditor` in `PaneRegistry`
- [x] Write UI test: note editor renders markdown preview
- [x] Write test: auto-save triggers after edit debounce

## 4.12 Highlights

- [x] Create `HighlightRepository` with `getHighlights(verseId: Long): List<Highlight>`, `saveHighlight()`, `deleteHighlight()`
- [x] Map `highlights` table to `Highlight` data class: `id`, `verseId`, `startOffset`, `endOffset`, `colorHex`, `label`, `createdAt`
- [x] Create `HighlightComponent` for managing highlight creation/deletion
- [x] Implement 8 highlight colors: yellow, green, blue, purple, pink, orange, red, gray
- [x] Create highlight overlay in Bible Reader: `AnnotatedString` spans with background color
- [x] Implement highlight label tagging and filtering by label
- [x] Implement long-press selection → highlight creation flow in BibleReaderPane
- [x] Register `highlights` in `PaneRegistry` (highlight manager pane)
- [x] Write test: highlight CRUD operations
- [x] Write UI test: highlight overlay renders on verse text

## 4.13 Bookmarks & History

- [x] Create `BookmarkRepository` with `getBookmarks(): List<Bookmark>`, `addBookmark(verseId, label)`, `removeBookmark(id)`
- [x] Map `bookmarks` table to `Bookmark` data class: `id`, `verseId`, `label`, `folderPath`, `createdAt`
- [x] Implement bookmark folders: create/rename/delete/move
- [x] Create `HistoryRepository` with `getHistory(limit: Int): List<HistoryEntry>`, `addEntry(verseId)`
- [x] Map `navigation_history` table to `HistoryEntry` data class
- [x] Cap history at 500 entries with automatic pruning
- [x] Create `BookmarksComponent` and `BookmarksPane` composable: folder tree + bookmark list
- [x] Create history view: chronological list with date headers
- [x] Implement bookmark/history tap: publish `LinkEvent.VerseSelected`
- [x] Register `bookmarks` in `PaneRegistry`
- [x] Write test: bookmark folder CRUD
- [x] Write test: history auto-prunes at 500 entries

---

## Phase 4 Exit Criteria

- [x] Word Study shows definition, occurrences, and frequency chart for any Strong's number
- [x] Interlinear displays original-language word grid with parsing and gloss
- [x] Reverse Interlinear maps English words back to original-language morphology
- [x] Passage Guide aggregates cross-refs, outlines, themes, and key words
- [x] Resource Library loads and displays commentary/dictionary entries
- [x] Note Editor supports Markdown editing with auto-save
- [x] Highlights render as colored overlays on Bible text
- [x] Bookmarks support folders; History auto-prunes
- [x] All study/writing module tests pass with ≥ 80% coverage
