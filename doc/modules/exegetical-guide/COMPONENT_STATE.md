# Exegetical Guide — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | Description |
|-----------|-------|-------------|
| `DefaultExegeticalGuideComponent` | Scoped | Aggregates 5 data sources for verse exegesis |

---

## 2. ExegeticalGuideComponent

### 2.1 Interface

```kotlin
interface ExegeticalGuideComponent {
    val state: StateFlow<ExegeticalGuideState>
    fun onVerseSelected(globalVerseId: Int)
    fun onCrossRefTapped(globalVerseId: Int)
    fun onStrongsTapped(strongsNumber: String)
}
```

### 2.2 State

```kotlin
data class ExegeticalGuideState(
    val loading: Boolean = false,
    val globalVerseId: Int? = null,
    val data: ExegeticalData? = null,
    val expandedSections: Set<Section> = Section.entries.toSet(),
    val error: AppError? = null,
)

enum class Section { Morphology, WordStudy, CrossReferences, Commentary, Context }
```

### 2.3 State Transitions

```
NoVerse --> Loading (VerseBus) --> Content (all sections)
                               +-> Partial (some sections)
                               +-> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | 5 parallel DB queries | Load all exegetical data |
| `onCrossRefTapped` | VerseBus publish | Navigate reader to cross-ref |
| `onStrongsTapped` | VerseBus publish | Open word study for Strong's number |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `VerseBus` | Bidirectional | SharedFlow | Subscribe to verse; publish cross-ref/Strong's |
| `MorphologyRepository` | → Reads | Koin DI | Word parsing |
| `WordStudyRepository` | → Reads | Koin DI | Lexicon entries |
| `CrossRefRepository` | → Reads | Koin DI | Cross-references |
| `ResourceRepository` | → Reads | Koin DI | Commentary |
| `BibleRepository` | → Reads | Koin DI | Context verses |

---

## 5. Component Registration (Koin)

```kotlin
val exegeticalGuideModule = module {
    factory<ExegeticalGuideComponent> { (ctx: ComponentContext) ->
        DefaultExegeticalGuideComponent(
            ctx, get(), get(), get(), get(), get(), get()
        )
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `loads all sections in parallel`() = runTest {
    val component = DefaultExegeticalGuideComponent(
        TestComponentContext(),
        FakeMorphRepo(), FakeWordStudyRepo(), FakeCrossRefRepo(),
        FakeResourceRepo(), FakeBibleRepo(), VerseBus()
    )
    component.onVerseSelected(43003016) // John 3:16
    component.state.test {
        val content = awaitItem()
        assertThat(content.data).isNotNull()
        assertThat(content.data!!.morphology).isNotEmpty()
        assertThat(content.data!!.crossReferences).isNotEmpty()
    }
}
```
