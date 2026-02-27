# Word Study — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultWordStudyComponent` | Scoped (per pane) | `shared/.../features/wordstudy/component/DefaultWordStudyComponent.kt` | Strong's lookup, occurrences, frequency |

---

## 2. WordStudyComponent

### 2.1 Interface

```kotlin
interface WordStudyComponent {
    val state: StateFlow<WordStudyState>
    fun onOccurrenceSelected(globalVerseId: Int)
    fun onSearchLexicon(query: String)
}
```

### 2.2 State

```kotlin
data class WordStudyState(
    val isLoading: Boolean = false,
    val entry: LexiconEntry? = null,
    val occurrences: List<WordOccurrence> = emptyList(),
    val occurrenceCount: Int = 0,
    val relatedWords: List<LexiconEntry> = emptyList(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no word selected)
  │
  │ VerseBus: StrongsSelected
  ▼
Loading (isLoading=true)
  │
  ├── success ──→ Content (entry, occurrences, relatedWords)
  │                │
  │                ├── onOccurrenceSelected(verseId) ──→ VerseBus publish
  │                └── VerseBus StrongsSelected ──→ Loading (new word)
  │
  └── failure ──→ Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `StrongsSelected` | DB queries | Loads lexicon entry + occurrences + related words |
| `onOccurrenceSelected` | VerseBus publish | Publishes `LinkEvent.VerseSelected(globalVerseId)` |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | ↔ Bidirectional | `SharedFlow<LinkEvent>` | Subscribes `StrongsSelected`; publishes `VerseSelected` |
| `MorphologyComponent` | ← Receives | VerseBus | Morphology pane triggers word study via `StrongsSelected` |
| `BibleReaderComponent` | ← Receives | VerseBus | Reader triggers via HTML Strong's links |

---

## 5. Testing

```kotlin
@Test
fun `StrongsSelected loads lexicon entry`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultWordStudyComponent(
        componentContext = TestComponentContext(),
        repository = FakeWordStudyRepository(testEntry, testOccurrences),
        verseBus = verseBus,
    )
    component.state.test {
        verseBus.publish(LinkEvent.StrongsSelected("H1254"))
        assertThat(awaitItem().isLoading).isTrue()
        val content = awaitItem()
        assertThat(content.entry?.strongsNumber).isEqualTo("H1254")
        assertThat(content.occurrences).isNotEmpty()
    }
}
```
