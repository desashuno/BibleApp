# Passage Guide — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultPassageGuideComponent` | Scoped (per pane) | `shared/.../features/passageguide/component/DefaultPassageGuideComponent.kt` | Aggregates data from 6 repositories |

---

## 2. PassageGuideComponent

### 2.1 Interface

```kotlin
interface PassageGuideComponent {
    val state: StateFlow<PassageGuideState>
    fun onRefSelected(crossRef: CrossReference)
    fun onWordSelected(strongsNumber: String)
    fun onSectionToggle(sectionId: String)
}
```

### 2.2 State

```kotlin
data class PassageGuideState(
    val isLoading: Boolean = false,
    val report: PassageReport? = null,
    val expandedSections: Set<String> = setOf("crossRefs", "commentary", "notes"),
    val loadingProgress: Map<String, Boolean> = emptyMap(),
    val error: AppError? = null,
)
```

### 2.3 State Transitions

```
Initial (no verse selected)
  |
  | VerseBus: VerseSelected
  v
Loading (isLoading=true, progressive section loading)
  |
  +-- sections arrive --> Content (report builds incrementally)
  |                       |
  |                       +-- onRefSelected(ref) --> VerseBus VerseSelected
  |                       +-- onWordSelected(s) --> VerseBus StrongsSelected
  |                       +-- VerseBus VerseSelected --> Loading (cancel + reload)
  |
  +-- all fail --> Error
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| VerseBus `VerseSelected` | 6 parallel DB queries | Builds PassageReport |
| `onRefSelected` | VerseBus publish | Publishes `VerseSelected(targetVerseId)` |
| `onWordSelected` | VerseBus publish | Publishes `StrongsSelected(strongsNumber)` |

---

## 4. Testing

```kotlin
@Test
fun `VerseSelected builds passage report from all repositories`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultPassageGuideComponent(
        componentContext = TestComponentContext(),
        bibleRepository = FakeBibleRepository(),
        crossRefRepository = FakeCrossRefRepository(testRefs),
        wordStudyRepository = FakeWordStudyRepository(),
        resourceRepository = FakeResourceRepository(testCommentary),
        noteRepository = FakeNoteRepository(testNotes),
        morphologyRepository = FakeMorphologyRepository(),
        verseBus = verseBus,
    )
    component.state.test {
        verseBus.publish(LinkEvent.VerseSelected(01001001))
        val content = awaitItem { it.report != null }
        assertThat(content.report!!.crossReferences).isEqualTo(testRefs)
    }
}
```
