# Sermon Editor — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultSermonEditorComponent` | Scoped | Sermon CRUD, section editing, auto-save |

---

## 2. SermonEditorComponent

### 2.1 Interface

```kotlin
interface SermonEditorComponent {
    val state: StateFlow<SermonEditorState>
    fun onLoad()
    fun onSermonSelected(sermonId: Long)
    fun onCreateSermon(title: String)
    fun onUpdateTitle(title: String)
    fun onUpdateSection(sectionId: Long, content: String)
    fun onAddSection(type: SectionType)
    fun onDeleteSection(sectionId: Long)
    fun onDeleteSermon(sermonId: Long)
    fun onBackToList()
    fun onSearchChanged(query: String)
}
```

### 2.2 State

```kotlin
data class SermonEditorState(
    val loading: Boolean = false,
    val sermons: List<Sermon> = emptyList(),
    val activeSermon: Sermon? = null,
    val sections: List<SermonSection> = emptyList(),
    val isSaving: Boolean = false,
    val searchQuery: String = "",
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> SermonList --> Editing --> (auto-save) --> Editing
                                 +-> Create   --> Editing
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onLoad` | DB query | Load sermons list |
| `onSermonSelected` | DB query | Load sermon + sections |
| `onUpdateSection` | Debounced DB write | Auto-save 1.5s after typing |
| `onCreateSermon` | DB insert | Create sermon + default sections |
| `onDeleteSermon` | DB cascade delete | Delete sermon + all sections |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Insert verse ref / navigate to verse |

---

## 5. Component Registration (Koin)

```kotlin
val sermonEditorModule = module {
    factory<SermonEditorComponent> { (ctx: ComponentContext) ->
        DefaultSermonEditorComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `auto-save persists after debounce`() = runTest {
    val repo = FakeSermonRepository()
    val component = DefaultSermonEditorComponent(TestComponentContext(), repo, VerseBus())
    component.onCreateSermon("Test Sermon")
    component.onUpdateSection(1L, "Updated content")
    advanceTimeBy(1600) // Past debounce
    assertThat(repo.lastSavedContent).isEqualTo("Updated content")
}
```
