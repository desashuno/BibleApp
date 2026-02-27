# Highlights — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultHighlightsComponent` | Scoped | Highlight CRUD, filtering, VerseBus sync |

---

## 2. HighlightsComponent

### 2.1 Interface

```kotlin
interface HighlightsComponent {
    val state: StateFlow<HighlightsState>
    fun onLoad()
    fun onCreateHighlight(globalVerseId: Int, colorIndex: Int, style: HighlightStyle)
    fun onDeleteHighlight(uuid: String)
    fun onFilterByColor(colorIndex: Int?)
    fun onHighlightSelected(highlight: Highlight)
}
```

### 2.2 State

```kotlin
data class HighlightsState(
    val loading: Boolean = false,
    val highlights: List<Highlight> = emptyList(),
    val colorFilter: Int? = null,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial --> Loading --> Content --> (create/delete) --> Content
                   +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | DB query | Load highlights for verse |
| `onCreateHighlight` | DB insert | Persist new highlight |
| `onDeleteHighlight` | DB soft delete | Mark as deleted |
| `onHighlightSelected` | VerseBus publish | Navigate reader to verse |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Subscribe + publish |
| `BibleReaderComponent` | → Reads | Koin DI | Overlay data source |

---

## 5. Component Registration (Koin)

```kotlin
val highlightsModule = module {
    factory<HighlightsComponent> { (ctx: ComponentContext) ->
        DefaultHighlightsComponent(ctx, get(), get())
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `onCreateHighlight adds to list`() = runTest {
    val component = DefaultHighlightsComponent(TestComponentContext(), FakeHighlightRepository(), VerseBus())
    component.state.test {
        component.onCreateHighlight(01001001, 0, HighlightStyle.Background)
        val state = awaitItem()
        assertThat(state.highlights).hasSize(1)
    }
}
```
