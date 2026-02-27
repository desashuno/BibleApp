# Note Editor

> Module documentation for **Note Editor** in BibleStudio.

---

## 1. Overview

The Note Editor module allows users to create, edit, and organize rich-text notes linked to specific Bible verses. Each note is uniquely identified by a UUID and associated with a `global_verse_id` (BBCCCVVV format), enabling quick navigation from any verse to its attached notes and vice versa.

Notes support basic rich-text formatting (bold, italic, headings, lists) via a custom Markdown-like editor. The module provides a full-text search capability through the `fts_notes` FTS5 virtual table, allowing notes to be discovered via the Search module.

---

## 2. Category & Pane

| Field | Value |
|-------|-------|
| **PaneType** | `PaneType.noteEditor` |
| **Category** | Writing |
| **Accent color** | Deep Blue `#4A6E8A` (`paneWriting`) |
| **Icon** | `Icons.edit_note` |

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
| `bible-reader` | Verse Bus | Subscribes to `VerseSelected` to load notes for verse |

### 4.2 Modules that depend on this (provides)

| Module | Dependency type | Description |
|--------|-------------------|-------------|
| `passage-guide` | Data | Passage guide reads user notes for verse |
| `search` | Data (FTS5) | Search queries `fts_notes` for note content |
| `import-export` | Data | Export includes user notes |

---

## 5. Key Source Files

| File | Layer | Purpose |
|---------|------|-----------|
| `shared/src/commonMain/kotlin/org/biblestudio/features/notes/component/NoteEditorComponent.kt` | Logic | Component interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/notes/component/DefaultNoteEditorComponent.kt` | Logic | Decompose implementation |
| `shared/src/commonMain/kotlin/org/biblestudio/features/notes/model/Note.kt` | Model | Note domain entity |
| `shared/src/commonMain/kotlin/org/biblestudio/features/notes/repository/NoteRepository.kt` | Data | Repository interface |
| `shared/src/commonMain/kotlin/org/biblestudio/features/notes/repository/NoteRepositoryImpl.kt` | Data | SQLDelight implementation |
| `composeApp/src/commonMain/kotlin/org/biblestudio/features/notes/ui/NoteEditorPane.kt` | UI | Rich-text editor pane |

---

## Module Documents

| Document | Contents |
|-----------|-----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Internal architecture, layers, data flow |
| [ROUTES.md](ROUTES.md) | Routes exposed and consumed |
| [DATA_MODEL.md](DATA_MODEL.md) | Entities, SQLite tables, repositories |
| [UI_COMPONENTS.md](UI_COMPONENTS.md) | Composables, PaneRegistry, wireframes |
| [COMPONENT_STATE.md](COMPONENT_STATE.md) | Components, StateFlow, side effects |
| [ROADMAP.md](ROADMAP.md) | Pending improvements |
