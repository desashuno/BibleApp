# Note Editor — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultNoteEditorComponent` | Scoped (per pane) | `shared/.../features/notes/component/DefaultNoteEditorComponent.kt` | Note CRUD with auto-save |

---

## 2. NoteEditorComponent

### 2.1 Interface

```kotlin
interface NoteEditorComponent {
    val state: StateFlow<NoteEditorState>
    fun onNoteSelected(uuid: String)
    fun onContentChanged(content: String)
    fun onTitleChanged(title: String)
    fun onCreateNote()
    fun onDeleteNote(uuid: String)
}
```

### 2.2 State

```kotlin
data class NoteEditorState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val activeNote: Note? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no verse selected)
  |
  | VerseBus: VerseSelected
  v
Loading (isLoading=true)
  |
  +-- success --> NoteList (notes for verse loaded)
  |                |
  |                +-- onNoteSelected(uuid) --> Editing (activeNote set)
  |                |                            |
  |                |                            +-- onContentChanged(c) --> Dirty (isDirty=true, debounce starts)
  |                |                            |                           |
  |                |                            |                           +-- 2s elapsed --> Saving --> Saved (isDirty=false)
  |                |                            +-- onDeleteNote(uuid) --> NoteList (note removed)
  |                |
  |                +-- onCreateNote() --> Editing (new note created + selected)
  |
  +-- failure --> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | DB query | Loads notes for verse |
| `onCreateNote` | DB insert | Inserts new Note with UUID |
| Content change + 2s debounce | DB update | Auto-saves note content |
| `onDeleteNote` | DB delete | Removes note (+ sync log entry) |

---

## 4. Testing

```kotlin
@Test
fun `auto-save triggers after 2s debounce`() = runTest {
    val component = DefaultNoteEditorComponent(
        componentContext = TestComponentContext(),
        repository = FakeNoteRepository(),
        verseBus = VerseBus(),
    )
    component.onCreateNote()
    component.onContentChanged("New content")
    advanceTimeBy(2000)
    component.state.test {
        val state = awaitItem()
        assertThat(state.isDirty).isFalse()
        assertThat(state.isSaving).isFalse()
    }
}
```
