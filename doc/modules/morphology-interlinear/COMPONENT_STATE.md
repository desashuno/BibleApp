# Morphology / Interlinear — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultInterlinearComponent` | Scoped (per pane) | `shared/.../features/morphology/component/DefaultInterlinearComponent.kt` | Interlinear display with VerseBus subscription |

---

## 2. InterlinearComponent

### 2.1 Interface

```kotlin
interface InterlinearComponent {
    val state: StateFlow<InterlinearState>
    fun onWordSelected(word: MorphWord)
    fun onDisplayModeChanged(mode: InterlinearDisplayMode)
}

enum class InterlinearDisplayMode { Interlinear, Parallel, Inline }
```

### 2.2 State

```kotlin
data class InterlinearState(
    val isLoading: Boolean = false,
    val verse: Int? = null,
    val words: List<MorphWord> = emptyList(),
    val displayMode: InterlinearDisplayMode = InterlinearDisplayMode.Interlinear,
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no verse selected)
  │
  │ VerseBus: VerseSelected
  ▼
Loading (isLoading=true)
  │
  ├── success ──→ Content (words populated, parsed)
  │                │
  │                ├── onWordSelected(w) ──→ VerseBus StrongsSelected
  │                ├── onDisplayModeChanged(m) ──→ Content (mode updated)
  │                └── VerseBus VerseSelected ──→ Loading (new verse)
  │
  └── failure ──→ Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | DB query | Loads morphology for verse |
| `onWordSelected` | VerseBus publish | Publishes `LinkEvent.StrongsSelected(strongsNumber)` |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | ↔ Bidirectional | `SharedFlow<LinkEvent>` | Subscribes `VerseSelected`; publishes `StrongsSelected` |
| `BibleReaderComponent` | ← Receives | VerseBus | Verse selection triggers morphology load |
| `WordStudyComponent` | → Triggers | VerseBus | Word tap triggers word study |

---

## 5. Testing

```kotlin
@Test
fun `VerseSelected loads morphology words in order`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultInterlinearComponent(
        componentContext = TestComponentContext(),
        repository = FakeMorphologyRepository(testWords),
        parsingDecoder = ParsingDecoder(),
        verseBus = verseBus,
    )
    component.state.test {
        verseBus.publish(LinkEvent.VerseSelected(globalVerseId = 01001001))
        assertThat(awaitItem().isLoading).isTrue()
        val content = awaitItem()
        assertThat(content.words).hasSize(10) // Gen 1:1
        assertThat(content.words.first().wordPosition).isEqualTo(1)
    }
}
```
