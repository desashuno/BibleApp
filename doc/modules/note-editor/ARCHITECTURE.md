# Note Editor — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  NoteEditorPane (@Composable)                     |
|  +-- RichTextEditor                               |
|  +-- NoteListPanel                                |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultNoteEditorComponent (Decompose)           |
|  +-- Manages StateFlow<NoteEditorState>           |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Auto-save with 2s debounce                   |
+---------------------------------------------------+
|                      DATA                         |
|  NoteRepository (interface)                       |
|  NoteRepositoryImpl                               |
|  +-- AnnotationQueries (SQLDelight)               |
|       +-- SQLite (notes, fts_notes)               |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Edit Note

1. **VerseBus event** or manual open -- Load notes for verse.
2. **User edits** -- Rich text changes update `NoteEditorState.content`.
3. **Auto-save (2s debounce)** -- After 2 seconds of inactivity, `NoteRepository.updateNote()` saves.
4. **FTS trigger** -- SQLite trigger updates `fts_notes` automatically.

### 2.2 Secondary Flows

- **New note** -- User taps "+" FAB -> inserts note with UUID, linked to current verse.
- **Delete note** -- Soft delete with confirmation dialog; `delete_log` entry for sync.
- **Note list** -- Shows all notes for current verse; tap to switch active note.

---

## 3. Dependency Injection

```kotlin
val noteEditorModule = module {
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultNoteEditorComponent(
            componentContext = ctx,
            repository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 4. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `NoteRepositoryImpl` | Abstracts note CRUD |
| Auto-save with debounce | Component (2s debounce) | Prevents data loss without excessive writes |
| UUID primary key | `notes.uuid` | Enables cross-device sync |
| FTS5 content sync | Triggers on notes table | Automatic full-text index maintenance |

---

## 5. Performance Considerations

- **Auto-save debounce (2s)**: Balances data safety with write reduction.
- **FTS sync via triggers**: Zero application-level FTS maintenance required.
- **UUID PK**: No auto-increment contention; safe for multi-device sync.

---

## 6. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| UUID primary key | Auto-increment | Required for offline-first sync between devices |
| 2s auto-save debounce | Manual save button, 5s debounce | 2s balances safety and write frequency |
| Rich text as Markdown-like | HTML, Delta format | Simpler parsing; human-readable export |
