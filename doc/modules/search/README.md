# Search

> Module documentation for **Search** in BibleStudio.

---

## 1. Overview

The Search module provides full-text search across all Bible text, user notes, resource entries, lexicon definitions, and sermon content using SQLite FTS5. It supports advanced query syntax including `AND`, `OR`, `NOT`, `NEAR`, and prefix matching, as well as book range and testament filters.

Search results are ranked by BM25 relevance and displayed with highlighted match snippets. The module also maintains a history of the last 20 queries for quick re-search. A Syntax Search sub-feature allows morphology-aware queries using a custom grammar (e.g. `[LEMMA:H1234]`, `[POS:Noun]`).

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.search` |
| **Category** | Tool |
| **Accent color** | Dusty Rose `#7A5A5A` (`paneTool`) |
| **Icon** | `Icons.search` |

---

## 3. Current Status

| Aspect | Status |
|---------|--------|
| SQLite Schema | Defined |
| Component(s) | Implemented |
| UI / Pane | Functional |
| Tests | Partial |
| i18n | Partial (EN/ES) |

---

## 4. Dependencies

### 4.1 Modules it depends on (consumes)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `bible-reader` | Data (FTS5) | Searches `fts_verses` virtual table backed by `verses` |
| `note-editor` | Data (FTS5) | Searches `fts_notes` virtual table backed by `notes` |
| `resource-library` | Data (FTS5) | Searches `fts_resources` virtual table backed by `resource_entries` |
| `word-study` | Data (FTS5) | Searches `fts_lexicon` virtual table backed by `lexicon_entries` |
| `sermon-editor` | Data (FTS5) | Searches `fts_sermons` virtual table backed by `sermon_sections` |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `bible-reader` | Verse Bus | Publishes `SearchResult` event → reader scrolls to result verse |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/search/component/SearchComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/search/component/DefaultSearchComponent.kt` | Logic | Decompose component implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/search/model/SearchResult.kt` | Model | Search result domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/search/repository/SearchRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/search/repository/SearchRepositoryImpl.kt` | Data | SQLDelight FTS5 implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/search/ui/SearchPane.kt` | UI | Main search pane |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/search/ui/SyntaxSearchPane.kt` | UI | Syntax search sub-feature |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed for inter-module communication |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories, queries |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, screens, PaneRegistry registration |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Decompose components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Prioritized pending improvements |
