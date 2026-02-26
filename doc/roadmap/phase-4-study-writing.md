# Phase 4 — Study & Writing Modules

> Word Study, Morphology/Interlinear, Reverse Interlinear, Passage Guide, Resource Library, Note Editor, Highlights, Bookmarks/History.
> **Prerequisites**: Phase 3 complete (Bible Reader, Search, Cross-References, Module System).

---

## 4.1 Word Study — Data Layer

- [ ] Create `WordStudyRepository` with `getStrongs(strongsId: String): StrongsEntry`
- [ ] Map `strongs` table to `StrongsEntry` data class: `id`, `lemma`, `transliteration`, `pronunciation`, `definition`, `usage`
- [ ] Implement `getOccurrences(strongsId: String): List<VerseOccurrence>` — query FTS `strongs_fts` table
- [ ] Implement `getRelatedWords(strongsId: String): List<StrongsEntry>` — semantic/root relations
- [ ] Create `WordStudyQuery.sq`
- [ ] Register repository in Koin
- [ ] Write test: Strong's entry resolves from ID
- [ ] Write test: occurrence count matches expected total

## 4.2 Word Study — Component & UI

- [ ] Create `WordStudyComponent` subscribing to VerseBus `StrongsSelected`
- [ ] Define `WordStudyState`: `entry`, `occurrences`, `relatedWords`, `isLoading`
- [ ] Implement occurrence frequency chart (bar chart by book)
- [ ] Create `WordStudyPane` composable: definition card + occurrence list + chart
- [ ] Implement occurrence tap: publish `LinkEvent.VerseSelected`
- [ ] Implement semantic domain grouping of occurrences
- [ ] Register `wordStudy` in `PaneRegistry`
- [ ] Write UI test: word study pane renders definition
- [ ] Write test: VerseBus subscription loads correct entry

## 4.3 Morphology / Interlinear — Data Layer

- [ ] Create `MorphologyRepository` with `getMorphology(verseId: Long): List<MorphWord>`
- [ ] Map `morphology` table to `MorphWord` data class: `surfaceForm`, `lemma`, `strongsId`, `parsing`, `gloss`, `position`
- [ ] Implement parsing decoder: `V-AAI-3S` → "Verb, Aorist, Active, Indicative, 3rd Person, Singular"
- [ ] Create `MorphologyQuery.sq` ordered by word position
- [ ] Register repository in Koin
- [ ] Write test: morphology returns words in positional order
- [ ] Write test: parsing decoder handles all POS tags

## 4.4 Morphology / Interlinear — Component & UI

- [ ] Create `InterlinearComponent` subscribing to VerseBus `VerseSelected`
- [ ] Define `InterlinearState`: `verse`, `words`, `displayMode`, `isLoading`
- [ ] Implement display modes: `Interlinear` (stacked), `Parallel` (side-by-side), `Inline` (tooltip)
- [ ] Create `InterlinearPane` composable: word grid with original + transliteration + gloss + parsing rows
- [ ] Implement word tap: publish `LinkEvent.StrongsSelected` to VerseBus
- [ ] Style Hebrew RTL text with proper bidirectional rendering
- [ ] Style Greek text with correct polytonic diacritics
- [ ] Register `interlinear` in `PaneRegistry`
- [ ] Write UI test: interlinear renders 4-row word grid
- [ ] Write test: word tap publishes StrongsSelected event

## 4.5 Reverse Interlinear

- [ ] Create `ReverseInterlinearComponent` that maps English words back to original language
- [ ] Implement alignment algorithm: match translation tokens to `morphology` entries via Strong's
- [ ] Create `ReverseInterlinearPane` composable: English text with underlined linked words
- [ ] Implement inline popover on tap: show original word, transliteration, parsing, definition
- [ ] Support multiple translation alignments (configurable base translation)
- [ ] Register `reverseInterlinear` in `PaneRegistry`
- [ ] Write test: alignment correctly maps English tokens to morphology entries
- [ ] Write UI test: tap on English word shows original language popover

## 4.6 Passage Guide — Data Layer

- [ ] Create `PassageGuideRepository` aggregating data from multiple repositories
- [ ] Implement `getGuide(verseId: Long): PassageGuide` — parallel query: cross-refs + outlines + themes + word studies
- [ ] Map `outlines` table to `Outline` data class: `title`, `points`, `sourceVerseRange`
- [ ] Create `PassageGuideQuery.sq` for outline and thematic queries
- [ ] Register repository in Koin
- [ ] Write test: passage guide aggregates all data sources

## 4.7 Passage Guide — Component & UI

- [ ] Create `PassageGuideComponent` subscribing to VerseBus `VerseSelected` / `PassageSelected`
- [ ] Define `PassageGuideState`: `verse`, `crossRefs`, `outlines`, `themes`, `keyWords`, `isLoading`
- [ ] Create `PassageGuidePane` composable: sectioned card layout (Outline, Cross-Refs, Key Words, Themes)
- [ ] Implement expandable sections with lazy loading
- [ ] Implement card tap navigation: cross-ref → VerseBus, word → StrongsSelected
- [ ] Register `passageGuide` in `PaneRegistry`
- [ ] Write UI test: passage guide renders all sections

## 4.8 Resource Library — Data Layer

- [ ] Create `ResourceRepository` with `getResources(type: ResourceType): List<Resource>`
- [ ] Map `resources` table to `Resource` data class: `id`, `title`, `author`, `type`, `moduleId`
- [ ] Implement `ResourceType` enum: `Commentary`, `Dictionary`, `Encyclopedia`, `DevotionalBook`
- [ ] Implement `getResourceEntry(resourceId, verseId): ResourceEntry` — look up commentary/dict entry for a verse
- [ ] Create `ResourceQuery.sq`
- [ ] Register repository in Koin
- [ ] Write test: resource entries resolve for verse range

## 4.9 Resource Library — Component & UI

- [ ] Create `ResourceLibraryComponent` subscribing to VerseBus `ResourceSelected` / `VerseSelected`
- [ ] Define `ResourceLibraryState`: `resources`, `activeResource`, `entry`, `isLoading`
- [ ] Create `ResourceLibraryPane` composable: sidebar resource list + content area with formatted entry
- [ ] Implement rich text rendering for commentary entries (bold, italic, links, footnotes)
- [ ] Implement resource search: filter/search within active resource
- [ ] Register `resourceLibrary` in `PaneRegistry`
- [ ] Write UI test: resource list shows installed commentaries
- [ ] Write test: VerseBus VerseSelected loads commentary for that verse

## 4.10 Note Editor — Data Layer

- [ ] Create `NoteRepository` with `getNotes(verseId: Long): List<Note>`, `saveNote(note: Note)`, `deleteNote(noteId: Long)`
- [ ] Map `notes` table to `Note` data class: `id`, `verseId`, `title`, `content`, `format`, `createdAt`, `updatedAt`
- [ ] Implement `NoteFormat` enum: `PlainText`, `Markdown`, `RichText`
- [ ] Implement `searchNotes(query: String): List<Note>` using `notes_fts` FTS5 table
- [ ] Create `NoteQuery.sq` with pagination support
- [ ] Register repository in Koin
- [ ] Write test: CRUD operations on notes
- [ ] Write test: FTS5 search returns matching notes

## 4.11 Note Editor — Component & UI

- [ ] Create `NoteEditorComponent` with `StateFlow<NoteEditorState>`
- [ ] Define `NoteEditorState`: `note`, `isDirty`, `isSaving`, `format`
- [ ] Implement Markdown editor with `BasicTextField` and live preview
- [ ] Implement rich text toolbar: bold, italic, heading, list, quote, link, verse reference
- [ ] Implement verse reference insertion: pick verse → insert `[Gen 1:1]` link
- [ ] Implement auto-save with 2-second debounce
- [ ] Create note list view with search and sort (date, title, verse)
- [ ] Register `noteEditor` in `PaneRegistry`
- [ ] Write UI test: note editor renders markdown preview
- [ ] Write test: auto-save triggers after edit debounce

## 4.12 Highlights

- [ ] Create `HighlightRepository` with `getHighlights(verseId: Long): List<Highlight>`, `saveHighlight()`, `deleteHighlight()`
- [ ] Map `highlights` table to `Highlight` data class: `id`, `verseId`, `startOffset`, `endOffset`, `colorHex`, `label`, `createdAt`
- [ ] Create `HighlightComponent` for managing highlight creation/deletion
- [ ] Implement 8 highlight colors: yellow, green, blue, purple, pink, orange, red, gray
- [ ] Create highlight overlay in Bible Reader: `AnnotatedString` spans with background color
- [ ] Implement highlight label tagging and filtering by label
- [ ] Implement long-press selection → highlight creation flow in BibleReaderPane
- [ ] Register `highlights` in `PaneRegistry` (highlight manager pane)
- [ ] Write test: highlight CRUD operations
- [ ] Write UI test: highlight overlay renders on verse text

## 4.13 Bookmarks & History

- [ ] Create `BookmarkRepository` with `getBookmarks(): List<Bookmark>`, `addBookmark(verseId, label)`, `removeBookmark(id)`
- [ ] Map `bookmarks` table to `Bookmark` data class: `id`, `verseId`, `label`, `folderPath`, `createdAt`
- [ ] Implement bookmark folders: create/rename/delete/move
- [ ] Create `HistoryRepository` with `getHistory(limit: Int): List<HistoryEntry>`, `addEntry(verseId)`
- [ ] Map `navigation_history` table to `HistoryEntry` data class
- [ ] Cap history at 500 entries with automatic pruning
- [ ] Create `BookmarksComponent` and `BookmarksPane` composable: folder tree + bookmark list
- [ ] Create history view: chronological list with date headers
- [ ] Implement bookmark/history tap: publish `LinkEvent.VerseSelected`
- [ ] Register `bookmarks` in `PaneRegistry`
- [ ] Write test: bookmark folder CRUD
- [ ] Write test: history auto-prunes at 500 entries

---

## Phase 4 Exit Criteria

- [ ] Word Study shows definition, occurrences, and frequency chart for any Strong's number
- [ ] Interlinear displays original-language word grid with parsing and gloss
- [ ] Reverse Interlinear maps English words back to original-language morphology
- [ ] Passage Guide aggregates cross-refs, outlines, themes, and key words
- [ ] Resource Library loads and displays commentary/dictionary entries
- [ ] Note Editor supports Markdown editing with auto-save
- [ ] Highlights render as colored overlays on Bible text
- [ ] Bookmarks support folders; History auto-prunes
- [ ] All study/writing module tests pass with ≥ 80% coverage
